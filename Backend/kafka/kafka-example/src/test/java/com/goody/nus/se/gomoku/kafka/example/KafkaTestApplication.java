package com.goody.nus.se.gomoku.kafka.example;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test Spring Boot application for MongoDB client
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan("com.goody.nus.se.gomoku")
public class KafkaTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTestApplication.class, args);
    }
}
