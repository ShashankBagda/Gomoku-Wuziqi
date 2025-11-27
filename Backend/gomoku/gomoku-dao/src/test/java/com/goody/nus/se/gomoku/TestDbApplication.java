package com.goody.nus.se.gomoku;

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
@MapperScan(basePackages = {"com.goody.nus.se.gomoku.gomoku.model.dao", "com.goody.nus.se.gomoku.gomoku.model.dao.customer"})
public class TestDbApplication {

}
