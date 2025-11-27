package com.goody.nus.se.gomoku.gomoku.matching;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.goody.nus.se.gomoku"
})
@MapperScan(basePackages = {"com.goody.nus.se.gomoku.gomoku.model.dao"})
@EnableMongoRepositories(basePackages = {
        "com.goody.nus.se.gomoku.gomoku.mongo.repository"
})
public class MatchTestApplication {
}
