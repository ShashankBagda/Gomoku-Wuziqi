package com.goody.nus.se.gomoku.ranking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

/**
 * test application
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.goody.nus.se.gomoku"})
@MapperScan(basePackages = {"com.goody.nus.se.gomoku.ranking.model.dao"})
@ActiveProfiles("test")
public class TestDbApplication {

}
