package com.autocoin.user.api;

import com.autocoin.global.config.JwtTokenProvider;
import com.autocoin.user.application.UserService;
import com.autocoin.user.domain.Role;
import com.autocoin.user.domain.User;
import com.autocoin.user.dto.UserLoginRequestDto;
import com.autocoin.user.dto.UserSignupRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 클래스의 통합 테스트
 * 
 * 이 테스트 클래스는 AuthController의 다음 API 엔드포인트를 검증합니다:
 * 1. /signup - 회원가입 API
 * 2. /login - 로그인 API
 * 3. /me - 인증된 사용자 정보 조회 API
 *
 * 각 테스트는 MockMvc를 사용하여 HTTP 요청을 시뮬레이션하고,
 * Given-When-Then 패턴을 따라 작성되었습니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String testToken;
    private UserSignupRequestDto signupRequestDto;
    private UserLoginRequestDto loginRequestDto;

    /**
     * 각 테스트 전에 실행되는 설정 메서드
     * MockMvc와 테스트 데이터를 초기화합니다.
     */
    @BeforeEach
    public void setup() {
        // MockMvc 설정 - Spring Security 적용
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트 사용자 설정
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .username("testuser")
                .role(Role.ROLE_USER)
                .build();

        // 테스트 토큰 설정
        testToken = "testJwtToken";

        // 회원가입 요청 DTO 설정
        signupRequestDto = UserSignupRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .username("testuser")
                .build();

        // 로그인 요청 DTO 설정
        loginRequestDto = UserLoginRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();
    }

    /**
     * 회원가입 API 테스트
     * 
     * 검증 내용:
     * - /signup 엔드포인트가 201 Created 상태 코드를 반환하는지 확인
     * - 응답 본문에 사용자 이메일과 이름이 올바르게 포함되어 있는지 확인
     */
    @Test
    @DisplayName("회원가입 API 테스트")
    public void testSignup() throws Exception {
        // Given: UserService가 회원가입 요청에 대해 테스트 사용자를 반환하도록 설정
        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(testUser);

        // When: /signup 엔드포인트로 POST 요청 실행
        ResultActions result = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequestDto)));

        // Then: 응답 상태 및 내용 검증
        result.andDo(print()) // 디버깅을 위한 응답 출력
                .andExpect(status().isCreated()) // 201 Created 상태 코드 기대
                .andExpect(jsonPath("$.email").value("test@example.com")) // 이메일 검증
                .andExpect(jsonPath("$.username").value("testuser")); // 사용자 이름 검증
    }

    /**
     * 로그인 API 테스트
     * 
     * 검증 내용:
     * - /login 엔드포인트가 200 OK 상태 코드를 반환하는지 확인
     * - 응답 본문에 JWT 토큰과 사용자 정보가 올바르게 포함되어 있는지 확인
     */
    @Test
    @DisplayName("로그인 API 테스트")
    public void testLogin() throws Exception {
        // Given: UserService와 JwtTokenProvider의 동작 설정
        List<String> roles = Collections.singletonList("ROLE_USER");
        when(userService.login(any(UserLoginRequestDto.class))).thenReturn(testUser);
        when(jwtTokenProvider.createToken(any(Long.class), any(String.class), any()))
                .thenReturn(testToken);

        // When: /login 엔드포인트로 POST 요청 실행
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)));

        // Then: 응답 상태 및 내용 검증
        result.andDo(print()) // 디버깅을 위한 응답 출력
                .andExpect(status().isOk()) // 200 OK 상태 코드 기대
                .andExpect(jsonPath("$.token").value(testToken)) // 토큰 검증
                .andExpect(jsonPath("$.user.email").value("test@example.com")) // 사용자 이메일 검증
                .andExpect(jsonPath("$.user.username").value("testuser")); // 사용자 이름 검증
    }

    /**
     * 내 정보 조회 API 테스트
     * 
     * 검증 내용:
     * - /me 엔드포인트가 인증된 요청에 대해 200 OK 상태 코드를 반환하는지 확인
     * - 응답 본문에 인증된 사용자 정보가 올바르게 포함되어 있는지 확인
     */
    @Test
    @DisplayName("내 정보 조회 API 테스트")
    public void testGetMe() throws Exception {
        // Given: Security Context에 인증 정보 설정
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // JWT 토큰 관련 동작 설정
        when(jwtTokenProvider.resolveToken(any())).thenReturn(testToken);
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(testToken)).thenReturn(auth);

        // When: /me 엔드포인트로 GET 요청 실행 (Authorization 헤더에 토큰 포함)
        ResultActions result = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + testToken));

        // Then: 응답 상태 및 내용 검증
        result.andDo(print()) // 디버깅을 위한 응답 출력
                .andExpect(status().isOk()) // 200 OK 상태 코드 기대
                .andExpect(jsonPath("$.email").value("test@example.com")) // 이메일 검증
                .andExpect(jsonPath("$.username").value("testuser")); // 사용자 이름 검증
    }

    /**
     * 내 정보 조회 API 테스트 - 인증 실패
     * 
     * 검증 내용:
     * - /me 엔드포인트가 인증되지 않은 요청에 대해 401 Unauthorized 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("내 정보 조회 API 테스트 - 인증 실패")
    public void testGetMe_Unauthorized() throws Exception {
        // Given: 토큰 없는 요청 설정
        when(jwtTokenProvider.resolveToken(any())).thenReturn(null);

        // When: /me 엔드포인트로 GET 요청 실행 (토큰 없음)
        ResultActions result = mockMvc.perform(get("/api/v1/auth/me"));

        // Then: 응답 상태 검증
        result.andDo(print()) // 디버깅을 위한 응답 출력
                .andExpect(status().isUnauthorized()); // 401 Unauthorized 상태 코드 기대
    }

    /**
     * 내 정보 조회 API 테스트 - 유효하지 않은 토큰
     * 
     * 검증 내용:
     * - /me 엔드포인트가 유효하지 않은 토큰을 가진 요청에 대해 401 Unauthorized 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("내 정보 조회 API 테스트 - 유효하지 않은 토큰")
    public void testGetMe_InvalidToken() throws Exception {
        // Given: 유효하지 않은 토큰 설정
        when(jwtTokenProvider.resolveToken(any())).thenReturn(testToken);
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(false);

        // When: /me 엔드포인트로 GET 요청 실행 (유효하지 않은 토큰 포함)
        ResultActions result = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + testToken));

        // Then: 응답 상태 검증
        result.andDo(print()) // 디버깅을 위한 응답 출력
                .andExpect(status().isUnauthorized()) // 401 Unauthorized 상태 코드 기대
                .andExpect(jsonPath("$.code").value("C001")) // 에러 코드 검증
                .andExpect(jsonPath("$.message").exists()); // 에러 메시지 존재 검증
    }
}
