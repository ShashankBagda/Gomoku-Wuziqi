package com.goody.nus.se.gomoku.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * test application
 *
 * @author Haotian
 * @version 1.0, 2025/9/11
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.goody.nus.se.gomoku"})
@MapperScan(basePackages = {"com.goody.nus.se.gomoku.user.model.dao"})
public class TestDbApplication {

}
