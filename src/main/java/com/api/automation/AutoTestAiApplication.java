package com.api.automation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.api.automation.mapper")
public class AutoTestAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoTestAiApplication.class, args);
    }
} 