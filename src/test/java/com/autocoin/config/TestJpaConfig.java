package com.autocoin.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * JPA 테스트 설정 클래스
 * 
 * WebMvcTest와 같은 슬라이스 테스트에서 JPA 관련 자동설정을 비활성화합니다.
 * 이를 통해 "JPA metamodel must not be empty" 오류를 방지합니다.
 */
@TestConfiguration
@Profile({"webmvc"})
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class TestJpaConfig {
    
    /**
     * 테스트용 PasswordEncoder Bean
     * 실제 객체를 사용하여 패스워드 인코딩 기능을 제공합니다.
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * EntityManager 모의 객체 생성
     * JPA 관련 요소를 모의 객체로 대체합니다.
     */
    @MockBean
    private jakarta.persistence.EntityManager entityManager;
    
    /**
     * EntityManagerFactory 모의 객체 생성
     * JPA 관련 요소를 모의 객체로 대체합니다.
     */
    @MockBean
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;
}
