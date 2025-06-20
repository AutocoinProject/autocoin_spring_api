package com.autocoin.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.autocoin.global.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Extract JWT token from Authorization header
        String token = jwtTokenProvider.resolveToken(request);
        
        try {
            // Validate token and set authentication if valid
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Set Authentication to security context for '{}', uri: {}", auth.getName(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            
            // Send unauthorized response
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .code("C001")
                    .message("Invalid token")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            log.error("Failed to process JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}
