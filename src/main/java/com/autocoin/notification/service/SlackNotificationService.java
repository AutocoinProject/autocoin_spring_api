package com.autocoin.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "slack.notifications.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {

    private final RestTemplate restTemplate;

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @Value("${slack.notifications.channels.errors:#errors}")
    private String errorChannel;

    @Value("${slack.notifications.channels.alerts:#alerts}")
    private String alertChannel;

    @Value("${slack.notifications.channels.trades:#trades}")
    private String tradeChannel;

    /**
     * 일반 메시지 전송
     */
    public void sendMessage(String channel, String message) {
        sendMessage(channel, message, null, null);
    }

    /**
     * 상세 메시지 전송
     */
    public void sendMessage(String channel, String message, String color, String title) {
        // Webhook URL 유효성 검사
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("https://hooks.slack.com/services/YOUR/WEBHOOK/URL")) {
            log.warn("Slack Webhook URL이 설정되지 않았습니다. 메시지 전송을 건너뛅니다.");
            return;
        }
        
        try {
            Map<String, Object> slackMessage = new HashMap<>();
            slackMessage.put("channel", channel);
            slackMessage.put("username", "AutoCoin Bot");
            slackMessage.put("icon_emoji", ":robot_face:");

            if (title != null || color != null) {
                // Attachment를 사용한 서식있는 메시지
                Map<String, Object> attachment = new HashMap<>();
                if (title != null) attachment.put("title", title);
                if (color != null) attachment.put("color", color);
                attachment.put("text", message);
                attachment.put("ts", System.currentTimeMillis() / 1000);
                
                slackMessage.put("attachments", new Object[]{attachment});
            } else {
                // 단순 텍스트 메시지
                slackMessage.put("text", message);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(slackMessage, headers);

            restTemplate.postForEntity(webhookUrl, entity, String.class);
            log.debug("Slack 메시지 전송 성공: {}", channel);

        } catch (Exception e) {
            log.error("Slack 메시지 전송 실패: {}", e.getMessage());
        }
    }

    /**
     * 에러 알림
     */
    public void sendErrorNotification(String title, String message, Exception exception) {
        String fullMessage = String.format("🚨 *%s*\\n\\n```%s```", title, message);
        
        if (exception != null) {
            fullMessage += String.format("\\n\\n*Exception:* `%s`", exception.getClass().getSimpleName());
            if (exception.getMessage() != null) {
                fullMessage += String.format("\\n*Message:* %s", exception.getMessage());
            }
        }
        
        fullMessage += String.format("\\n\\n*Time:* %s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(errorChannel, fullMessage, "danger", "Application Error");
    }

    /**
     * 성공 알림
     */
    public void sendSuccessNotification(String title, String message) {
        String fullMessage = String.format("✅ *%s*\\n\\n%s\\n\\n*Time:* %s", 
            title, 
            message,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(alertChannel, fullMessage, "good", "Success");
    }

    /**
     * 경고 알림
     */
    public void sendWarningNotification(String title, String message) {
        String fullMessage = String.format("⚠️ *%s*\\n\\n%s\\n\\n*Time:* %s", 
            title, 
            message,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(alertChannel, fullMessage, "warning", "Warning");
    }

    /**
     * 거래 알림
     */
    public void sendTradeNotification(String market, String type, String price, String amount) {
        String emoji = type.equalsIgnoreCase("BUY") ? "📈" : "📉";
        String color = type.equalsIgnoreCase("BUY") ? "good" : "danger";
        
        String message = String.format("%s *%s %s*\\n\\n", emoji, type.toUpperCase(), market);
        message += String.format("*Price:* %s\\n", price);
        message += String.format("*Amount:* %s\\n", amount);
        message += String.format("*Time:* %s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(tradeChannel, message, color, String.format("%s Trade Alert", market));
    }

    /**
     * 애플리케이션 시작 알림
     */
    public void sendStartupNotification() {
        String message = "🚀 *AutoCoin Application Started*\\n\\n";
        message += String.format("*Environment:* %s\\n", getEnvironment());
        message += String.format("*Start Time:* %s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(alertChannel, message, "good", "Application Startup");
    }

    /**
     * 애플리케이션 종료 알림
     */
    public void sendShutdownNotification() {
        String message = "🛑 *AutoCoin Application Shutdown*\\n\\n";
        message += String.format("*Environment:* %s\\n", getEnvironment());
        message += String.format("*Shutdown Time:* %s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(alertChannel, message, "warning", "Application Shutdown");
    }

    /**
     * 상태 체크 알림
     */
    public void sendHealthCheckNotification(String status, Map<String, Object> details) {
        String emoji = "UP".equals(status) ? "✅" : "❌";
        String color = "UP".equals(status) ? "good" : "danger";
        
        String message = String.format("%s *Health Check: %s*\\n\\n", emoji, status);
        
        if (details != null && !details.isEmpty()) {
            message += "*Details:*\\n";
            for (Map.Entry<String, Object> entry : details.entrySet()) {
                message += String.format("• %s: %s\\n", entry.getKey(), entry.getValue());
            }
        }
        
        message += String.format("\\n*Time:* %s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        sendMessage(alertChannel, message, color, "Health Check");
    }

    private String getEnvironment() {
        return System.getProperty("spring.profiles.active", "unknown");
    }
}
