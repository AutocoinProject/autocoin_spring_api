package com.autocoin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 테스트 환경에서 스케줄링을 비활성화하는 Configuration
 */
@TestConfiguration
@Profile({"test", "webmvc"})
public class TestSchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 스케줄러를 설정하지 않아서 @Scheduled 어노테이션이 동작하지 않도록 함
    }
}
