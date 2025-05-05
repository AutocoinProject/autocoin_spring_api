package com.autocoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AutocoinSpringApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutocoinSpringApiApplication.class, args);
    }
}
