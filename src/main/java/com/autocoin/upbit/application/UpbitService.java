package com.autocoin.upbit.application;

import com.autocoin.global.exception.CustomException;
import com.autocoin.global.exception.ErrorCode;
import com.autocoin.upbit.domain.UpbitAccountRepository;
import com.autocoin.upbit.domain.entity.UpbitAccount;
import com.autocoin.upbit.dto.UpbitAccountInfoDto;
import com.autocoin.upbit.dto.UpbitTickerDto;
import com.autocoin.upbit.dto.request.UpbitConnectRequestDto;
import com.autocoin.upbit.dto.response.UpbitAccountStatusResponseDto;
import com.autocoin.upbit.dto.response.UpbitConnectResponseDto;
import com.autocoin.upbit.dto.response.WalletResponseDto;
import com.autocoin.upbit.infrastructure.UpbitApiClient;
import com.autocoin.user.domain.User;
import com.autocoin.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UpbitService {
    
    private final UpbitAccountRepository upbitAccountRepository;
    private final UserRepository userRepository;
    private final UpbitApiClient upbitApiClient;
    private final UpbitCryptoService upbitCryptoService;
    private final UpbitAuthService upbitAuthService;
    
    /**
     * 업비트 계정 연결
     */
    public UpbitConnectResponseDto connectUpbitAccount(UpbitConnectRequestDto request, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        try {
            // API 키 유효성 검증
            boolean isValid = upbitAuthService.validateApiKeys(request.getAccessKey(), request.getSecretKey());
            
            if (!isValid) {
                throw new CustomException(ErrorCode.INVALID_UPBIT_API_KEYS);
            }
            
            // API 키 암호화
            String encryptedAccessKey = upbitCryptoService.encrypt(request.getAccessKey());
            String encryptedSecretKey = upbitCryptoService.encrypt(request.getSecretKey());
            
            // 기존 계정 확인
            Optional<UpbitAccount> existingAccount = upbitAccountRepository.findByUser(user);
            
            UpbitAccount upbitAccount;
            if (existingAccount.isPresent()) {
                upbitAccount = existingAccount.get();
                upbitAccount.updateApiKeys(encryptedAccessKey, encryptedSecretKey);
                if (request.getNickname() != null) {
                    upbitAccount.updateNickname(request.getNickname());
                }
            } else {
                upbitAccount = UpbitAccount.builder()
                        .user(user)
                        .encryptedAccessKey(encryptedAccessKey)
                        .encryptedSecretKey(encryptedSecretKey)
                        .accountState(UpbitAccount.AccountState.ACTIVE)
                        .lastSyncAt(LocalDateTime.now())
                        .nickname(request.getNickname())
                        .build();
            }
            
            upbitAccountRepository.save(upbitAccount);
            
            return UpbitConnectResponseDto.builder()
                    .success(true)
                    .message("업비트 계정이 성공적으로 연결되었습니다.")
                    .accountState(upbitAccount.getAccountState().name())
                    .nickname(upbitAccount.getNickname())
                    .build();
                    
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("업비트 계정 연결 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.UPBIT_CONNECTION_FAILED);
        }
    }
    
    /**
     * 업비트 계정 상태 조회
     */
    @Transactional(readOnly = true)
    public UpbitAccountStatusResponseDto getUpbitAccountStatus(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Optional<UpbitAccount> upbitAccount = upbitAccountRepository.findByUser(user);
        
        if (upbitAccount.isPresent()) {
            UpbitAccount account = upbitAccount.get();
            return UpbitAccountStatusResponseDto.builder()
                    .connected(true)
                    .accountState(account.getAccountState().name())
                    .lastSyncAt(account.getLastSyncAt())
                    .nickname(account.getNickname())
                    .build();
        } else {
            return UpbitAccountStatusResponseDto.builder()
                    .connected(false)
                    .accountState("NONE")
                    .build();
        }
    }
    
    /**
     * 지갑 정보 조회
     */
    @Transactional(readOnly = true)
    public List<WalletResponseDto> getWalletInfo(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        UpbitAccount upbitAccount = upbitAccountRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.UPBIT_ACCOUNT_NOT_FOUND));
        
        if (!upbitAccount.isActive()) {
            throw new CustomException(ErrorCode.UPBIT_ACCOUNT_INACTIVE);
        }
        
        try {
            // API 키 복호화
            String accessKey = upbitCryptoService.decrypt(upbitAccount.getEncryptedAccessKey());
            String secretKey = upbitCryptoService.decrypt(upbitAccount.getEncryptedSecretKey());
            
            // 업비트 API 호출
            List<UpbitAccountInfoDto> accounts = upbitApiClient.getAccounts(accessKey, secretKey);
            
            // 응답 DTO 변환
            return accounts.stream()
                    .map(account -> WalletResponseDto.builder()
                            .currency(account.getCurrency())
                            .balance(account.getBalance())
                            .locked(account.getLocked())
                            .avgBuyPrice(account.getAvgBuyPrice())
                            .avgBuyPriceModified(account.isAvgBuyPriceModified())
                            .unitCurrency(account.getUnitCurrency())
                            .build())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("지갑 정보 조회 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.UPBIT_API_ERROR);
        }
    }
    
    /**
     * 암호화폐 시세 조회
     */
    @Transactional(readOnly = true)
    public List<UpbitTickerDto> getMarketTickers(List<String> markets) {
        try {
            return upbitApiClient.getTickers(markets);
        } catch (Exception e) {
            log.error("시세 정보 조회 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.UPBIT_API_ERROR);
        }
    }
    
    /**
     * 업비트 계정 연결 해제
     */
    public void disconnectUpbitAccount(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        UpbitAccount upbitAccount = upbitAccountRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.UPBIT_ACCOUNT_NOT_FOUND));
        
        upbitAccount.deactivate();
        upbitAccountRepository.save(upbitAccount);
        
        log.info("사용자 {}의 업비트 계정 연결이 해제되었습니다.", userEmail);
    }
    
    /**
     * 계정 상태 동기화
     */
    @Transactional
    public void syncAccountStatus(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        UpbitAccount upbitAccount = upbitAccountRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.UPBIT_ACCOUNT_NOT_FOUND));
        
        try {
            // API 키 복호화
            String accessKey = upbitCryptoService.decrypt(upbitAccount.getEncryptedAccessKey());
            String secretKey = upbitCryptoService.decrypt(upbitAccount.getEncryptedSecretKey());
            
            // API 키 유효성 재검증
            boolean isValid = upbitAuthService.validateApiKeys(accessKey, secretKey);
            
            if (isValid) {
                upbitAccount.updateAccountState(UpbitAccount.AccountState.ACTIVE);
            } else {
                upbitAccount.updateAccountState(UpbitAccount.AccountState.ERROR);
            }
            
            upbitAccountRepository.save(upbitAccount);
            
        } catch (Exception e) {
            log.error("계정 상태 동기화 실패: {}", e.getMessage());
            upbitAccount.updateAccountState(UpbitAccount.AccountState.ERROR);
            upbitAccountRepository.save(upbitAccount);
            throw new CustomException(ErrorCode.UPBIT_SYNC_FAILED);
        }
    }
}