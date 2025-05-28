package com.autocoin.global.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Value("${spring.datasource.url:NOT_SET}")
    private String dbUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String dbUsername;

    @Value("${spring.profiles.active:NOT_SET}")
    private String activeProfile;

    @Value("${DB_URL:NOT_SET}")
    private String envDbUrl;

    @GetMapping("/env")
    public Map<String, String> getEnvironment() {
        Map<String, String> env = new HashMap<>();
        env.put("profile", activeProfile);
        env.put("datasource.url", dbUrl);
        env.put("datasource.username", dbUsername);
        env.put("env.DB_URL", envDbUrl);
        env.put("system.DB_URL", System.getenv("DB_URL"));
        env.put("system.DB_USERNAME", System.getenv("DB_USERNAME"));
        env.put("java.version", System.getProperty("java.version"));
        return env;
    }
}
