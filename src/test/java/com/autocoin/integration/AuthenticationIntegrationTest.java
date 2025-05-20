package com.autocoin.integration;

import com.autocoin.global.util.PasswordEncoderUtil;
import com.autocoin.user.domain.Role;
import com.autocoin.user.domain.User;
import com.autocoin.user.domain.UserRepository;
import com.autocoin.user.dto.UserLoginRequestDto;
import com.autocoin.user.dto.UserSignupRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JWT 인증 시스템의 통합 테스트
 * 
 * 이 테스트 클래스는 실제 데이터베이스 연결을 통해 다음 인증 흐름을 검증합니다:
 * 1. 회원가입 - 로그인 - 내 정보 조회 통합 플로우
 * 2. 잘못된 비밀번호로 인한 로그인 실패
 * 3. 인증 없이 보호된 API 접근 실패
 * 4. 만료된 토큰으로 접근 실패
 *
 * 테스트 실행을 위해 H2 인메모리 데이터베이스와 테스트 프로파일을 사용합니다.
 * 트랜잭션은 각 테스트 완료 후 롤백됩니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // 테스트 프로파일 활성화
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    private UserSignupRequestDto signupRequestDto;
    private UserLoginRequestDto loginRequestDto;

    /**
     * 각 테스트 전에 실행되는 설정 메서드
     * 테스트 데이터를 초기화하고, 중복 방지를 위해 기존 테스트 사용자를 삭제합니다.
     */
    @BeforeEach
    public void setup() {
        // 테스트 전에 기존 사용자 삭제 (이메일 중복 방지)
        try {
            userRepository.findByEmail("integrationtest@example.com")
                    .ifPresent(user -> userRepository.delete(user));
        } catch (Exception e) {
            // 테이블이 아직 생성되지 않았을 경우 무시
        }

        // 회원가입 요청 DTO 설정
        signupRequestDto = UserSignupRequestDto.builder()
                .email("integrationtest@example.com")
                .password("IntegrationTest123!")
                .username("tester")
                .build();

        // 로그인 요청 DTO 설정
        loginRequestDto = UserLoginRequestDto.builder()
                .email("integrationtest@example.com")
                .password("IntegrationTest123!")
                .build();
    }

    /**
     * 회원가입-로그인-내정보 조회 통합 테스트
     * 
     * 검증 내용:
     * - 회원가입이 성공하고 201 Created 상태 코드를 반환하는지 확인
     * - 로그인이 성공하고 200 OK 상태 코드와 JWT 토큰을 반환하는지 확인
     * - 발급된 토큰으로 내 정보 조회가 성공하고 200 OK 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("회원가입-로그인-내정보 조회 통합 테스트")
    @Transactional
    public void fullAuthenticationFlow() throws Exception {
        // 1. Given & When: 회원가입 요청 실행
        ResultActions signupResult = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequestDto)));

        // Then: 회원가입 응답 검증
        signupResult.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integrationtest@example.com"))
                .andExpect(jsonPath("$.username").value("tester"));

        // 2. Given & When: 로그인 요청 실행
        ResultActions loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)));

        // Then: 로그인 응답 검증 및 토큰 추출
        MvcResult mvcResult = loginResult.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("tester"))
                .andExpect(jsonPath("$.user.email").value("integrationtest@example.com"))
                .andReturn();

        // 응답에서 토큰 추출
        String responseContent = mvcResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        String token = (String) responseMap.get("token");

        // 토큰 유효성 검증
        assertNotNull(token, "토큰은 null이 아니어야 합니다");
        assertTrue(token.length() > 0, "토큰은 빈 문자열이 아니어야 합니다");

        // 3. Given & When: 추출한 토큰으로 내 정보 조회 요청 실행
        ResultActions meResult = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token));

        // Then: 내 정보 조회 응답 검증
        meResult.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integrationtest@example.com"))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    /**
     * 로그인 실패 테스트 - 잘못된 비밀번호
     * 
     * 검증 내용:
     * - 잘못된 비밀번호로 로그인 시 401 Unauthorized 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    @Transactional
    public void loginFailure_WrongPassword() throws Exception {
        // Given: 사용자 미리 생성
        User user = User.builder()
                .email("wrongpassword@example.com")
                .password(passwordEncoderUtil.encode("CorrectPassword123!"))
                .username("wrongpass")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        // 잘못된 비밀번호로 로그인 요청 DTO 설정
        UserLoginRequestDto wrongPasswordLoginRequest = UserLoginRequestDto.builder()
                .email("wrongpassword@example.com")
                .password("WrongPassword123!")
                .build();

        // When: 잘못된 비밀번호로 로그인 요청 실행
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordLoginRequest)));

        // Then: 로그인 실패 응답 검증
        result.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 인증 없이 보호된 경로 접근 시 실패 테스트
     * 
     * 검증 내용:
     * - 인증 없이 /me 엔드포인트 접근 시 401 Unauthorized 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("인증 없이 보호된 경로 접근 시 실패 테스트")
    @Transactional
    public void accessProtectedRouteWithoutAuth() throws Exception {
        // Given: 인증 정보 없음

        // When: 인증 없이 /me 엔드포인트 접근
        ResultActions result = mockMvc.perform(get("/api/v1/auth/me"));

        // Then: 접근 실패 응답 검증
        result.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * 만료된 토큰으로 접근 시 실패 테스트
     * 
     * 검증 내용:
     * - 만료된 토큰으로 /me 엔드포인트 접근 시 401 Unauthorized 상태 코드를 반환하는지 확인
     */
    @Test
    @DisplayName("만료된 토큰으로 접근 시 실패 테스트")
    @Transactional
    public void accessWithExpiredToken() throws Exception {
        // Given: 만료된 토큰 (형식만 맞춘 임의의 토큰)
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxNTc1MTU0MDAwfQ.P7xxiHds1qMNvJWMLx-xyLuYlGBfxF8SIcDbbcgr_qI";

        // When: 만료된 토큰으로 /me 엔드포인트 접근
        ResultActions result = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + expiredToken));

        // Then: 접근 실패 응답 검증
        result.andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
