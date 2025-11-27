package com.goody.nus.se.gomoku.gomoku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.goody.nus.se.gomoku"
})
@EnableMongoRepositories(basePackages = {
        "com.goody.nus.se.gomoku.gomoku.mongo.repository"
})
public class ControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }

}
