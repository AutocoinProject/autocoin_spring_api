package com.autocoin.global.config.security;

import com.autocoin.global.exception.ErrorResponse;
import com.autocoin.user.oauth.CustomOAuth2UserService;
import com.autocoin.user.oauth.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions().disable()) // H2 콘솔을 위한 프레임 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    // 인증되지 않은 요청에 대해 401 응답
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    
                                    ErrorResponse errorResponse = ErrorResponse.builder()
                                            .status(HttpServletResponse.SC_UNAUTHORIZED)
                                            .code("C001")
                                            .message("Unauthorized access")
                                            .timestamp(LocalDateTime.now())
                                            .build();
                                    
                                    objectMapper.writeValue(response.getOutputStream(), errorResponse);
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    // 권한 없는 요청에 대해 403 응답
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    
                                    ErrorResponse errorResponse = ErrorResponse.builder()
                                            .status(HttpServletResponse.SC_FORBIDDEN)
                                            .code("C002")
                                            .message("Access denied")
                                            .timestamp(LocalDateTime.now())
                                            .build();
                                    
                                    objectMapper.writeValue(response.getOutputStream(), errorResponse);
                                })
                )
                .authorizeHttpRequests(auth -> auth
                        // 공개 API 엔드포인트
                        .requestMatchers("/", "/health", "/api/health").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup").permitAll()
                        .requestMatchers("/api/v1/auth/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/authorization/**").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // H2 Database Console (개발용)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Spring Boot Actuator 엔드포인트들 (모니터링)
                        .requestMatchers("/actuator/**").permitAll()
                        // 테스트 API (개발용)
                        .requestMatchers("/api/v1/test/**").permitAll()
                        // Slack 테스트 API
                        .requestMatchers("/api/v1/slack/**").permitAll()
                        // 모니터링 API
                        .requestMatchers("/api/monitoring/**").permitAll()
                        // 나머지 API는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        // OAuth2 로그인 페이지 경로 설정
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization"))
                        )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, objectMapper), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
