package com.autocoin.notification.api;

import com.autocoin.notification.service.SlackNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/slack")
@RequiredArgsConstructor
@ConditionalOnBean(SlackNotificationService.class)
@Tag(name = "Slack Notifications", description = "Slack 알림 API")
public class SlackController {

    private final SlackNotificationService slackNotificationService;

    @GetMapping("/status")
    @Operation(summary = "Slack 연동 상태 확인", description = "Slack Webhook 설정 상태를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getSlackStatus() {
        return ResponseEntity.ok(Map.of(
            "webhook_configured", slackNotificationService != null,
            "service_available", slackNotificationService != null,
            "message", slackNotificationService != null ? "Slack 서비스가 활성화되어 있습니다." : "Slack 서비스가 비활성화되어 있습니다."
        ));
    }

    @PostMapping("/test")
    @Operation(summary = "Slack 연결 테스트", description = "Slack Webhook 연결을 테스트합니다.")
    public ResponseEntity<Map<String, Object>> testSlackConnection() {
        try {
            slackNotificationService.sendMessage(
                "#general", 
                "✅ AutoCoin Slack 연동 테스트가 성공했습니다!"
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Slack 테스트 메시지가 전송되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Slack 연동에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/send")
    @Operation(summary = "사용자 정의 메시지 전송", description = "지정된 채널에 사용자 정의 메시지를 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendCustomMessage(
            @Parameter(description = "채널명 (예: #general)")
            @RequestParam String channel,
            @Parameter(description = "전송할 메시지")
            @RequestParam String message) {
        try {
            slackNotificationService.sendMessage(channel, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "메시지가 전송되었습니다.",
                "channel", channel
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "메시지 전송에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/alert")
    @Operation(summary = "알림 메시지 전송", description = "성공/경고/에러 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendAlert(
            @Parameter(description = "알림 타입 (success, warning, error)")
            @RequestParam String type,
            @Parameter(description = "알림 제목")
            @RequestParam String title,
            @Parameter(description = "알림 내용")
            @RequestParam String message) {
        try {
            switch (type.toLowerCase()) {
                case "success":
                    slackNotificationService.sendSuccessNotification(title, message);
                    break;
                case "warning":
                    slackNotificationService.sendWarningNotification(title, message);
                    break;
                case "error":
                    slackNotificationService.sendErrorNotification(title, message, null);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "잘못된 알림 타입입니다. (success, warning, error 중 선택)"
                    ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", type.toUpperCase() + " 알림이 전송되었습니다.",
                "type", type
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "알림 전송에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/trade-alert")
    @Operation(summary = "거래 알림 전송", description = "거래 완료 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendTradeAlert(
            @Parameter(description = "마켓 (예: KRW-BTC)")
            @RequestParam String market,
            @Parameter(description = "거래 타입 (BUY, SELL)")
            @RequestParam String type,
            @Parameter(description = "가격")
            @RequestParam String price,
            @Parameter(description = "수량")
            @RequestParam String amount) {
        try {
            slackNotificationService.sendTradeNotification(market, type, price, amount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "거래 알림이 전송되었습니다.",
                "market", market,
                "type", type
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "거래 알림 전송에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}
