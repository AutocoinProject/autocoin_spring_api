package com.autocoin.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderUtil {

    private final PasswordEncoder passwordEncoder;

    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        // OAuth2 사용자는 비밀번호가 비어있을 수 있음
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
