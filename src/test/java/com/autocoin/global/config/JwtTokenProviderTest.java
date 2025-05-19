package com.autocoin.global.config;

import com.autocoin.global.config.security.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JwtTokenProvider 클래스의 단위 테스트
 * 
 * 이 테스트 클래스는 JwtTokenProvider의 다음 기능을 검증합니다:
 * 1. JWT 토큰 생성
 * 2. 토큰에서 사용자 정보(이메일, ID) 추출
 * 3. 토큰 유효성 검증 - 유효한 토큰, 만료된 토큰, 잘못된 토큰
 * 4. 토큰으로부터 인증 정보 조회
 * 5. HTTP 요청에서 토큰 추출
 *
 * ReflectionTestUtils를 사용하여 비공개 필드를 직접 설정하고,
 * Given-When-Then 패턴을 따라 각 테스트가 작성되었습니다.
 */
@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String secretKey = "testSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKey";
    private long tokenValidTime = 30 * 60 * 1000L; // 30분
    private Long userId = 1L;
    private String email = "test@example.com";
    private List<String> roles = Collections.singletonList("ROLE_USER");

    /**
     * 각 테스트 전에 실행되는 설정 메서드
     * JwtTokenProvider의 비공개 필드를 설정하고 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        // Given: JwtTokenProvider의 비공개 필드를 ReflectionTestUtils를 사용하여 직접 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "tokenValidTime", tokenValidTime);
        jwtTokenProvider.init(); // 키 초기화 메소드 호출
    }

    /**
     * JWT 토큰 생성 테스트
     * 
     * 검증 내용:
     * - createToken 메서드가 null이 아닌 유효한 토큰을 생성하는지 확인
     * - 생성된 토큰의 길이가 0보다 큰지 확인
     */
    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void createToken_Success() {
        // When: JWT 토큰을 생성
        String token = jwtTokenProvider.createToken(userId, email, roles);

        // Then: 생성된 토큰이 유효한지 검증
        assertNotNull(token, "토큰은 null이 아니어야 합니다");
        assertTrue(token.length() > 0, "토큰은 빈 문자열이 아니어야 합니다");
    }

    /**
     * JWT 토큰에서 이메일 추출 테스트
     * 
     * 검증 내용:
     * - 토큰에서 추출한 이메일이 토큰 생성 시 사용한 이메일과 일치하는지 확인
     */
    @Test
    @DisplayName("JWT 토큰에서 이메일 추출 테스트")
    void getEmail_Success() {
        // Given: 유효한 JWT 토큰
        String token = jwtTokenProvider.createToken(userId, email, roles);

        // When: 토큰에서 이메일을 추출
        String extractedEmail = jwtTokenProvider.getEmail(token);

        // Then: 추출된 이메일이 원래 이메일과 일치하는지 검증
        assertEquals(email, extractedEmail, "추출된 이메일은 원래 이메일과 일치해야 합니다");
    }

    /**
     * JWT 토큰에서 사용자 ID 추출 테스트
     * 
     * 검증 내용:
     * - 토큰에서 추출한 사용자 ID가 토큰 생성 시 사용한 ID와 일치하는지 확인
     */
    @Test
    @DisplayName("JWT 토큰에서 사용자 ID 추출 테스트")
    void getUserId_Success() {
        // Given: 유효한 JWT 토큰
        String token = jwtTokenProvider.createToken(userId, email, roles);

        // When: 토큰에서 사용자 ID를 추출
        Long extractedId = jwtTokenProvider.getUserId(token);

        // Then: 추출된 사용자 ID가 원래 ID와 일치하는지 검증
        assertEquals(userId, extractedId, "추출된 사용자 ID는 원래 ID와 일치해야 합니다");
    }

    /**
     * JWT 토큰 유효성 검증 테스트 - 유효한 토큰
     * 
     * 검증 내용:
     * - 만료되지 않은 정상적인 토큰이 유효하다고 판단되는지 확인
     */
    @Test
    @DisplayName("JWT 토큰 유효성 검증 테스트 - 유효한 토큰")
    void validateToken_ValidToken() {
        // Given: 유효한 JWT 토큰
        String token = jwtTokenProvider.createToken(userId, email, roles);

        // When: 토큰 유효성 검증
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then: 토큰이 유효한지 검증
        assertTrue(isValid, "유효한 토큰은 검증을 통과해야 합니다");
    }

    /**
     * JWT 토큰 유효성 검증 테스트 - 만료된 토큰
     * 
     * 검증 내용:
     * - 만료된 토큰이 유효하지 않다고 판단되는지 확인
     */
    @Test
    @DisplayName("JWT 토큰 유효성 검증 테스트 - 만료된 토큰")
    void validateToken_ExpiredToken() {
        // Given: 토큰 유효 시간을 1ms로 설정하여 바로 만료되는 토큰 생성
        ReflectionTestUtils.setField(jwtTokenProvider, "tokenValidTime", 1L);
        String token = jwtTokenProvider.createToken(userId, email, roles);

        // 토큰이 만료되도록 잠시 대기
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: 만료된 토큰 유효성 검증
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then: 만료된 토큰은 유효하지 않음을 검증
        assertFalse(isValid, "만료된 토큰은 유효하지 않아야 합니다");
    }

    /**
     * JWT 토큰 유효성 검증 테스트 - 잘못된 토큰
     * 
     * 검증 내용:
     * - 형식이 잘못된 토큰이 유효하지 않다고 판단되는지 확인
     */
    @Test
    @DisplayName("JWT 토큰 유효성 검증 테스트 - 잘못된 토큰")
    void validateToken_InvalidToken() {
        // Given: 형식이 잘못된 토큰
        String invalidToken = "invalid.token.string";

        // When: 잘못된 토큰 유효성 검증
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then: 잘못된 토큰은 유효하지 않음을 검증
        assertFalse(isValid, "잘못된 형식의 토큰은 유효하지 않아야 합니다");
    }

    /**
     * JWT 토큰으로부터 인증 정보 조회 테스트
     * 
     * 검증 내용:
     * - 토큰으로부터 유효한 Authentication 객체를 얻을 수 있는지 확인
     * - 생성된 Authentication 객체의 name이 토큰에 포함된 이메일과 일치하는지 확인
     */
    @Test
    @DisplayName("JWT 토큰으로부터 인증 정보 조회 테스트")
    void getAuthentication_Success() {
        // Given: 유효한 JWT 토큰과 UserDetails 모의 객체 설정
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(userDetails);

        String token = jwtTokenProvider.createToken(userId, email, roles);

        // When: 토큰으로부터 인증 정보 조회
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // Then: 인증 정보가 유효한지 검증
        assertNotNull(authentication, "인증 객체는 null이 아니어야 합니다");
        assertEquals(email, authentication.getName(), "인증 객체의 이름은 토큰의 이메일과 일치해야 합니다");
    }

    /**
     * HTTP 요청에서 토큰 추출 테스트
     * 
     * 검증 내용:
     * - HTTP 요청의 Authorization 헤더에서 Bearer 토큰을 올바르게 추출하는지 확인
     */
    @Test
    @DisplayName("HTTP 요청에서 토큰 추출 테스트")
    void resolveToken_Success() {
        // Given: Authorization 헤더에 Bearer 토큰이 포함된 HTTP 요청 모의 객체 설정
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = jwtTokenProvider.createToken(userId, email, roles);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // When: HTTP 요청에서 토큰 추출
        String resolvedToken = jwtTokenProvider.resolveToken(request);

        // Then: 추출된 토큰이 원래 토큰과 일치하는지 검증
        assertEquals(token, resolvedToken, "추출된 토큰은 원래 토큰과 일치해야 합니다");
    }

    /**
     * HTTP 요청에서 토큰 추출 테스트 - Authorization 헤더 없음
     * 
     * 검증 내용:
     * - Authorization 헤더가 없는 HTTP 요청에서 null을 반환하는지 확인
     */
    @Test
    @DisplayName("HTTP 요청에서 토큰 추출 테스트 - Authorization 헤더 없음")
    void resolveToken_NoAuthorizationHeader() {
        // Given: Authorization 헤더가 없는 HTTP 요청 모의 객체 설정
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When: HTTP 요청에서 토큰 추출
        String resolvedToken = jwtTokenProvider.resolveToken(request);

        // Then: 추출된 토큰이 null인지 검증
        assertNull(resolvedToken, "Authorization 헤더가 없는 경우 null을 반환해야 합니다");
    }

    /**
     * HTTP 요청에서 토큰 추출 테스트 - Bearer 형식이 아닌 경우
     * 
     * 검증 내용:
     * - Authorization 헤더가 Bearer 형식이 아닌 HTTP 요청에서 null을 반환하는지 확인
     */
    @Test
    @DisplayName("HTTP 요청에서 토큰 추출 테스트 - Bearer 형식이 아닌 경우")
    void resolveToken_NotBearerFormat() {
        // Given: Bearer 형식이 아닌 Authorization 헤더가 있는 HTTP 요청 모의 객체 설정
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = jwtTokenProvider.createToken(userId, email, roles);
        when(request.getHeader("Authorization")).thenReturn("Token " + token);

        // When: HTTP 요청에서 토큰 추출
        String resolvedToken = jwtTokenProvider.resolveToken(request);

        // Then: 추출된 토큰이 null인지 검증
        assertNull(resolvedToken, "Bearer 형식이 아닌 경우 null을 반환해야 합니다");
    }
}
