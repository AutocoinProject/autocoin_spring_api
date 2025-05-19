package com.autocoin.global.config;

import com.autocoin.global.config.security.JwtTokenProvider;
import com.autocoin.global.config.security.JwtAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * JwtAuthenticationFilter 클래스의 단위 테스트
 * 
 * 이 테스트 클래스는 JwtAuthenticationFilter의 JWT 토큰 인증 처리 기능을 검증합니다:
 * 1. 유효한 토큰이 있을 때 인증 처리
 * 2. 토큰이 없을 때 인증 처리
 * 3. 유효하지 않은 토큰일 때 인증 처리
 * 4. 토큰 처리 중 예외 발생 시 처리
 *
 * 각 테스트는 Mock 객체를 사용하여 외부 의존성을 격리하고,
 * Given-When-Then 패턴을 따라 작성되었습니다.
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 각 테스트 전에 실행되는 설정 메서드
     * JwtAuthenticationFilter 객체를 초기화하고 SecurityContext를 비웁니다.
     */
    @BeforeEach
    void setUp() {
        // 각 테스트 전에 필터 객체와 보안 컨텍스트 초기화
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    /**
     * 유효한 토큰이 있을 때 인증 처리 테스트
     * 
     * 검증 내용:
     * - 유효한 토큰이 있을 때 JwtTokenProvider를 통해 인증 정보를 가져오는지 확인
     * - FilterChain이 올바르게 계속 실행되는지 확인
     */
    @Test
    @DisplayName("유효한 토큰이 있을 때 인증 처리 테스트")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // Given: 유효한 토큰 설정
        String token = "valid-token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(token)).willReturn(authentication);

        // When: 필터 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: 필터체인이 계속 실행되고, 인증 정보가 올바르게 처리되는지 검증
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, times(1)).getAuthentication(token);
        // SecurityContextHolder에 설정된 인증 정보는 static 컨텍스트이므로 직접 검증하기 어려움
    }

    /**
     * 토큰이 없을 때 인증 처리 테스트
     * 
     * 검증 내용:
     * - 토큰이 없을 때 인증 처리를 하지 않고 FilterChain이 계속 실행되는지 확인
     * - SecurityContext에 인증 정보가 설정되지 않는지 확인
     */
    @Test
    @DisplayName("토큰이 없을 때 인증 처리 테스트")
    void doFilterInternal_NoToken() throws ServletException, IOException {
        // Given: 토큰이 없는 상황 설정
        given(jwtTokenProvider.resolveToken(request)).willReturn(null);

        // When: 필터 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: 필터체인이 계속 실행되고, 인증 처리가 호출되지 않는지 검증
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
                  "토큰이 없는 경우 SecurityContext에 인증 정보가 설정되지 않아야 합니다");
    }

    /**
     * 유효하지 않은 토큰일 때 인증 처리 테스트
     * 
     * 검증 내용:
     * - 유효하지 않은 토큰이 있을 때 인증 처리를 하지 않고 FilterChain이 계속 실행되는지 확인
     * - SecurityContext에 인증 정보가 설정되지 않는지 확인
     */
    @Test
    @DisplayName("유효하지 않은 토큰일 때 인증 처리 테스트")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // Given: 유효하지 않은 토큰 설정
        String token = "invalid-token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        // When: 필터 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: 필터체인이 계속 실행되고, 인증 처리가 호출되지 않는지 검증
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                  "유효하지 않은 토큰의 경우 SecurityContext에 인증 정보가 설정되지 않아야 합니다");
    }

    /**
     * 토큰 처리 중 예외 발생 테스트
     * 
     * 검증 내용:
     * - 토큰 처리 중 예외가 발생해도 FilterChain이 계속 실행되는지 확인
     * - SecurityContext가 비워지는지 확인
     */
    @Test
    @DisplayName("토큰 처리 중 예외 발생 테스트")
    void doFilterInternal_ExceptionOccurred() throws ServletException, IOException {
        // Given: 토큰 검증 중 예외 발생 설정
        String token = "exception-token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(jwtTokenProvider.validateToken(token)).willThrow(new RuntimeException("Token validation error"));

        // When: 필터 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: 예외가 발생해도 필터체인이 계속 실행되고, 보안 컨텍스트가 비워지는지 검증
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                  "예외 발생 시 SecurityContext가 비워져야 합니다");
    }
}
