package com.autocoin.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 테스트 환경에서 Entity와 Repository를 스캔하는 설정
 * WebMvcTest에서 JPA 메타모델 오류를 해결하기 위한 설정입니다.
 */
@TestConfiguration
@Profile("test")
@EntityScan(basePackages = "com.autocoin.post.domain.entity")
@EnableJpaRepositories(basePackages = "com.autocoin.post.infrastructure.repository",
    includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Repository")
)
public class TestEntityConfig {
    // Configuration for entity scanning in tests
}
