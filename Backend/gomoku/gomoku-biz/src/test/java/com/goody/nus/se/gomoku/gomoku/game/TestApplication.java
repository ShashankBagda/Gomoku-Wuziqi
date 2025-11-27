package com.goody.nus.se.gomoku.gomoku.game;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * run application for testing
 *
 * @author Goody
 * @version 1.0, 2022/5/6
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan("com.goody.nus.se.gomoku")
@MapperScan(basePackages = {"com.goody.nus.se.gomoku.gomoku.model.dao"})
@EnableMongoRepositories(basePackages = {"com.goody.nus.se.gomoku.gomoku.mongo.repository"})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
