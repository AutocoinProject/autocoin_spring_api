package com.autocoin.notification.listener;

import com.autocoin.notification.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(SlackNotificationService.class)
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventListener {

    private final SlackNotificationService slackNotificationService;

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("애플리케이션 시작 완료 - Slack 알림 전송");
        slackNotificationService.sendStartupNotification();
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        log.info("애플리케이션 종료 - Slack 알림 전송");
        slackNotificationService.sendShutdownNotification();
    }
}
