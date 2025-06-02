package com.autocoin.global.api;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
@Tag(name = "테스트", description = "Sentry 및 모니터링 테스트 API")
public class TestController {

    @Operation(summary = "Sentry 에러 테스트", description = "Sentry에 에러를 전송하는 테스트입니다.")
    @PostMapping("/sentry-error")
    public ResponseEntity<Map<String, String>> testSentryError(
            @Parameter(description = "에러 타입", example = "runtime")
            @RequestParam(defaultValue = "runtime") String errorType) {
        
        log.info("Sentry 에러 테스트 시작: errorType={}", errorType);
        log.info("Sentry 상태 확인: enabled={}, dsn={}", Sentry.isEnabled(), 
                 Sentry.getCurrentHub().getOptions().getDsn());
        
        // uncaught 타입은 바로 예외를 던짐 (catch하지 않음)
        if ("uncaught".equalsIgnoreCase(errorType)) {
            log.info("Uncaught exception 테스트 - 예외를 바로 던집니다.");
            throw new RuntimeException("Uncaught exception for Sentry auto-capture test!");
        }
        
        try {
            switch (errorType.toLowerCase()) {
                case "runtime":
                    throw new RuntimeException("Sentry 테스트용 Runtime Exception 발생!");
                
                case "npe":
                    String nullString = null;
                    int length = nullString.length(); // NPE 발생
                    break;
                
                case "array":
                    int[] array = {1, 2, 3};
                    int value = array[10]; // ArrayIndexOutOfBounds 발생
                    break;
                
                case "custom":
                    throw new IllegalArgumentException("Sentry 테스트용 Custom Exception 발생!");
                
                default:
                    throw new RuntimeException("알 수 없는 에러 타입: " + errorType);
            }
        } catch (Exception e) {
            // Sentry 상태 재확인
            boolean sentryEnabled = Sentry.isEnabled();
            String dsn = Sentry.getCurrentHub().getOptions().getDsn();
            
            log.info("예외 발생 시 Sentry 상태: enabled={}, dsn={}", sentryEnabled, dsn);
            
            if (sentryEnabled && dsn != null && !dsn.isEmpty()) {
                // Sentry에 수동으로 추가 정보와 함께 전송
                try {
                    Sentry.withScope(scope -> {
                        scope.setTag("test_type", "manual_sentry_test");
                        scope.setTag("error_type", errorType);
                        scope.setLevel(io.sentry.SentryLevel.ERROR);
                        scope.setExtra("endpoint", "/api/v1/test/sentry-error");
                        scope.setExtra("purpose", "Sentry 연동 테스트");
                        scope.setExtra("timestamp", String.valueOf(System.currentTimeMillis()));
                        Sentry.captureException(e);
                    });
                    
                    log.info("Sentry로 예외 전송 완료");
                } catch (Exception sentryError) {
                    log.error("Sentry 전송 중 오류 발생: {}", sentryError.getMessage(), sentryError);
                }
            } else {
                log.warn("Sentry가 비활성화되어 있거나 DSN이 설정되지 않음");
            }
            
            log.error("Sentry 테스트 에러 발생: {}", e.getMessage(), e);
            
            return ResponseEntity.ok(Map.of(
                "status", sentryEnabled ? "error_sent_to_sentry" : "sentry_disabled",
                "message", "에러가 " + (sentryEnabled ? "Sentry로 전송되었습니다" : "발생했지만 Sentry가 비활성화됨") + ": " + e.getMessage(),
                "errorType", errorType,
                "sentryEnabled", String.valueOf(sentryEnabled),
                "sentryDsn", dsn != null ? "configured" : "not_configured",
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "no_error",
            "message", "에러가 발생하지 않았습니다"
        ));
    }

    @Operation(summary = "Sentry 메시지 테스트", description = "Sentry에 일반 메시지를 전송하는 테스트입니다.")
    @PostMapping("/sentry-message")
    public ResponseEntity<Map<String, String>> testSentryMessage(
            @Parameter(description = "메시지 내용", example = "테스트 메시지")
            @RequestParam(defaultValue = "Sentry 테스트 메시지") String message) {
        
        log.info("Sentry 메시지 테스트: {}", message);
        
        // Sentry에 일반 메시지 전송
        Sentry.withScope(scope -> {
            scope.setTag("test_type", "message_test");
            scope.setLevel(io.sentry.SentryLevel.INFO);
            scope.setExtra("endpoint", "/api/v1/test/sentry-message");
            scope.setExtra("custom_message", message);
            scope.setExtra("timestamp", String.valueOf(System.currentTimeMillis()));
            Sentry.captureMessage("테스트 메시지: " + message);
        });
        
        return ResponseEntity.ok(Map.of(
            "status", "message_sent",
            "message", "메시지가 Sentry로 전송되었습니다: " + message,
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @Operation(summary = "Sentry 상태 확인 (상세)", description = "Sentry 설정 상태를 자세히 확인합니다.")
    @GetMapping("/sentry-status")
    public ResponseEntity<Map<String, Object>> getSentryStatus() {
        
        boolean sentryEnabled = Sentry.isEnabled();
        String dsn = Sentry.getCurrentHub().getOptions().getDsn();
        String environment = Sentry.getCurrentHub().getOptions().getEnvironment();
        String release = Sentry.getCurrentHub().getOptions().getRelease();
        Double sampleRate = Sentry.getCurrentHub().getOptions().getTracesSampleRate();
        
        log.info("=== Sentry 상태 확인 ===");
        log.info("Sentry Enabled: {}", sentryEnabled);
        log.info("DSN: {}", dsn);
        log.info("Environment: {}", environment);
        log.info("Release: {}", release);
        log.info("Sample Rate: {}", sampleRate);
        log.info("Hub: {}", Sentry.getCurrentHub());
        log.info("========================");
        
        return ResponseEntity.ok(Map.of(
            "sentry_enabled", sentryEnabled,
            "dsn_configured", dsn != null && !dsn.isEmpty(),
            "dsn_host", dsn != null ? dsn.split("@")[1].split("/")[0] : "not_configured",
            "environment", environment != null ? environment : "not_set",
            "release", release != null ? release : "not_set",
            "traces_sample_rate", sampleRate != null ? sampleRate : 0.0,
            "status", sentryEnabled ? "active" : "inactive",
            "hub_active", Sentry.getCurrentHub() != null,
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @Operation(summary = "헬스 체크", description = "API 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "service", "Autocoin API",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @Operation(summary = "Sentry 직접 테스트", description = "Sentry에 직접 메시지를 전송하는 단순 테스트입니다.")
    @PostMapping("/sentry-direct-test")
    public ResponseEntity<Map<String, Object>> testSentryDirect() {
        
        log.info("Sentry 직접 테스트 시작");
        
        boolean sentryEnabled = Sentry.isEnabled();
        String dsn = Sentry.getCurrentHub().getOptions().getDsn();
        
        log.info("Sentry 상태: enabled={}, dsn={}", sentryEnabled, dsn);
        
        if (sentryEnabled) {
            try {
                // 단순 메시지 전송
                Sentry.captureMessage("테스트 메시지: Sentry 직접 테스트 성공!");
                log.info("Sentry 메시지 전송 완료");
                
                // 예외도 전송
                try {
                    throw new RuntimeException("테스트용 예외: Sentry 직접 테스트!");
                } catch (Exception e) {
                    Sentry.captureException(e);
                    log.info("Sentry 예외 전송 완료");
                }
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Sentry로 메시지와 예외가 전송되었습니다.",
                    "sentry_enabled", true,
                    "dsn_configured", dsn != null && !dsn.isEmpty(),
                    "timestamp", String.valueOf(System.currentTimeMillis())
                ));
                
            } catch (Exception e) {
                log.error("Sentry 전송 중 오류: {}", e.getMessage(), e);
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "Sentry 전송 중 오류가 발생했습니다: " + e.getMessage(),
                    "sentry_enabled", true,
                    "dsn_configured", dsn != null && !dsn.isEmpty(),
                    "timestamp", String.valueOf(System.currentTimeMillis())
                ));
            }
        } else {
            log.warn("Sentry가 비활성화됨");
            return ResponseEntity.ok(Map.of(
                "status", "disabled",
                "message", "Sentry가 비활성화되어 있습니다.",
                "sentry_enabled", false,
                "dsn_configured", dsn != null && !dsn.isEmpty(),
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        }
    }

    @Operation(summary = "Trading API 테스트", description = "Trading API 엔드포인트 상태를 확인합니다.")
    @GetMapping("/trading")
    public ResponseEntity<Map<String, Object>> testTrading() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "Trading API ready",
            "timestamp", String.valueOf(System.currentTimeMillis()),
            "endpoints", Map.of(
                "start", "POST /api/v1/trading/start",
                "stop", "POST /api/v1/trading/stop", 
                "status", "GET /api/v1/trading/status",
                "health", "GET /api/v1/trading/health",
                "notify", "POST /api/v1/trading/notify"
            ),
            "required_auth", Map.of(
                "start_stop_status", "JWT Bearer Token 필요",
                "health_notify", "인증 불필요"
            )
        ));
    }

    @Operation(summary = "Sentry 강제 초기화", description = "Sentry를 강제로 초기화합니다.")
    @PostMapping("/sentry-force-init")
    public ResponseEntity<Map<String, Object>> forceSentryInit() {
        
        log.info("Sentry 강제 초기화 시작");
        
        // 하드코딩된 DSN 사용
        String hardcodedDsn = "https://7118a3d1e7fb2437d01c62780e465e9c@o4509382423871488.ingest.us.sentry.io/4509382429704192";
        
        try {
            // Sentry Options 설정
            io.sentry.SentryOptions options = new io.sentry.SentryOptions();
            options.setDsn(hardcodedDsn);
            options.setEnvironment("local");
            options.setRelease("1.0.0");
            options.setTracesSampleRate(1.0); // 100% 샘플링
            options.setAttachStacktrace(true);
            options.setAttachThreads(true);
            options.setEnableTracing(true);
            options.setDebug(true);
            
            // 민감한 정보 필터링
            options.setBeforeSend((event, hint) -> {
                if (event.getRequest() != null && event.getRequest().getHeaders() != null) {
                    event.getRequest().getHeaders().remove("Authorization");
                    event.getRequest().getHeaders().remove("Cookie");
                }
                return event;
            });
            
            // Sentry 초기화
            Sentry.init(options);
            
            // 초기화 후 상태 확인
            boolean sentryEnabled = Sentry.isEnabled();
            String dsn = Sentry.getCurrentHub().getOptions().getDsn();
            
            log.info("강제 초기화 완료: enabled={}, dsn={}", sentryEnabled, dsn);
            
            if (sentryEnabled) {
                // 테스트 메시지 전송
                Sentry.captureMessage("강제 초기화 후 테스트 메시지!");
                
                // 테스트 예외 전송
                try {
                    throw new RuntimeException("강제 초기화 후 테스트 예외!");
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
                
                log.info("Sentry 테스트 메시지 및 예외 전송 완료");
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sentry 강제 초기화 성공",
                "sentry_enabled", sentryEnabled,
                "dsn_configured", dsn != null && !dsn.isEmpty(),
                "dsn_value", dsn != null ? dsn.substring(0, Math.min(30, dsn.length())) + "..." : "null",
                "environment", Sentry.getCurrentHub().getOptions().getEnvironment(),
                "release", Sentry.getCurrentHub().getOptions().getRelease(),
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
            
        } catch (Exception e) {
            log.error("Sentry 강제 초기화 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Sentry 강제 초기화 실패: " + e.getMessage(),
                "error_details", e.getClass().getSimpleName(),
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        }
    }
}
