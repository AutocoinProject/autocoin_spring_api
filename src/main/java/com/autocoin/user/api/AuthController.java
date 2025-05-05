package com.autocoin.user.api;

import com.autocoin.global.config.JwtTokenProvider;
import com.autocoin.user.application.UserService;
import com.autocoin.user.domain.User;
import com.autocoin.user.dto.UserLoginRequestDto;
import com.autocoin.user.dto.UserResponseDto;
import com.autocoin.user.dto.UserSignupRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody UserSignupRequestDto requestDto) {
        User user = userService.signup(requestDto);
        return new ResponseEntity<>(UserResponseDto.of(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        User user = userService.login(requestDto);
        
        // Role 정보를 List<String>으로 변환
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), roles);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", UserResponseDto.of(user));
        
        return ResponseEntity.ok(response);
    }
}
