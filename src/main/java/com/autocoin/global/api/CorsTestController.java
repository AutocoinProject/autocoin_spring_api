package com.autocoin.global.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cors-test")
public class CorsTestController {

    @GetMapping
    public ResponseEntity<Map<String, String>> corsTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS is working properly!");
        return ResponseEntity.ok(response);
    }
}
