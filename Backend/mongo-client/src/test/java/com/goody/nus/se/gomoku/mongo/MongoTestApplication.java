package com.goody.nus.se.gomoku.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Test Spring Boot application for MongoDB client
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.goody.nus.se.gomoku.mongo.repository")
public class MongoTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoTestApplication.class, args);
    }
}
