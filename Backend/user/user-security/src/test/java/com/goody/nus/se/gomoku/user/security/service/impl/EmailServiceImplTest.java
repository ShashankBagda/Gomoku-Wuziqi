package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.redis.service.RedisService;
import com.goody.nus.se.gomoku.user.security.VerifyTest;
import com.goody.nus.se.gomoku.user.security.service.IEmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = VerifyTest.class)
public class EmailServiceImplTest {
    @Autowired
    private IEmailService emailService;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void clearRedisKeys() {
        for (String key : redisService.keys("email:verify:*")) {
            redisService.delete(key);
        }
    }

    @Test
    @Disabled
    void testSendVerificationCode_StoresCodeInRedis() {
        String email = "1962249400@qq.com";

        // method under test
        emailService.sendVerificationCode(email);

        // verify code is stored in Redis
        String redisKey = "email:verify:" + email;
        Object code = redisService.get(redisKey);

        assertThat(code)
                .as("验证码应该存入 Redis")
                .isNotNull()
                .matches(c -> c.toString().matches("\\d{6}"));

        boolean b = emailService.verifyCode(email, code.toString());
        Assertions.assertTrue(b);
        Assertions.assertNull(redisService.get(redisKey));
    }

    @Test
    void testVerifyCode_Successful() {
        String email = "1962249400@qq.com";
        String code = "123456";

        // pre-store code in Redis
        redisService.set("email:verify:" + email, code, 5, TimeUnit.MINUTES);

        boolean result = emailService.verifyCode(email, "123456");

        assertThat(result).isTrue();
        assertThat(redisService.exists("email:verify:" + email))
                .as("验证成功后应删除 Redis 记录")
                .isFalse();
    }

    @Test
    void testVerifyCode_Fail_WrongCode() {
        String email = "1962249400@qq.com";
        redisService.set("email:verify:" + email, "999999", 5, TimeUnit.MINUTES);

        boolean result = emailService.verifyCode(email, "000000");

        assertThat(result).isFalse();
    }

    @Test
    void testVerifyCode_Fail_Expired() {
        String email = "1962249400@qq.com";

        boolean result = emailService.verifyCode(email, "111111");

        assertThat(result).isFalse();
    }
}
