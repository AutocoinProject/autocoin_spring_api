package com.autocoin.config;

import com.amazonaws.services.s3.AmazonS3;
import com.autocoin.global.config.external.S3Config;
import com.autocoin.file.application.service.S3Uploader;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 테스트용 Configuration 클래스
 * 
 * 실제 AWS S3 연결 대신 Mock 객체를 제공하여 테스트 환경에서
 * S3 관련 의존성 문제를 해결합니다.
 */
@TestConfiguration
@Profile({"test", "webmvc"})
@Import({TestSchedulingConfig.class, TestJpaConfig.class, TestEntityConfig.class})
public class TestConfig {

    /**
     * 테스트용 Mock AmazonS3 Bean
     * 실제 S3 서비스 대신 Mock 객체를 제공합니다.
     */
    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        return Mockito.mock(AmazonS3.class);
    }

    /**
     * 테스트용 Mock S3Uploader Bean
     * 실제 파일 업로드 대신 Mock 객체를 제공합니다.
     */
    @Bean
    @Primary
    public S3Uploader s3Uploader() {
        return Mockito.mock(S3Uploader.class);
    }

    /**
     * 테스트용 Mock S3Config Bean
     * 실제 S3 설정 대신 Mock 객체를 제공합니다.
     */
    @Bean
    @Primary
    public S3Config s3Config() {
        return Mockito.mock(S3Config.class);
    }
    
    /**
     * PostService Mock Bean 제공
     * 컨트롤러 테스트에서 서비스 계층을 모의 객체로 대체합니다.
     */
    @Bean
    @Primary
    public com.autocoin.post.application.service.PostService postService() {
        return Mockito.mock(com.autocoin.post.application.service.PostService.class);
    }
}
