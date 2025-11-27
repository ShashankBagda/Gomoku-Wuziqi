package com.goody.nus.se.gomoku.gomoku.matching.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * run application for testing
 *
 * @author Goody
 * @version 1.0, 2025/10/16
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan("com.goody.nus.se.gomoku")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
