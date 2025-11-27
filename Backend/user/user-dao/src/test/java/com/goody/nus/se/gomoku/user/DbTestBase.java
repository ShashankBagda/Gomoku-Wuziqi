package com.goody.nus.se.gomoku.user;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * base class
 *
 * @author Haotian
 * @version 1.0, 2025/9/11
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestDbApplication.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DbTestBase {
}
