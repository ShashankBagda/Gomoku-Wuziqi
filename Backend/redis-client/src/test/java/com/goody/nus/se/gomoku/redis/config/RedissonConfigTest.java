package com.goody.nus.se.gomoku.redis.config;

import com.goody.nus.se.gomoku.redis.TestApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedissonConfig} test
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@SpringBootTest(classes = TestApplication.class)
@ContextConfiguration
class RedissonConfigTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedissonProperties redissonProperties;

    @Test
    void test_redissonClient_notNull() {
        assertNotNull(redissonClient);
        assertFalse(redissonClient.isShutdown());
        assertFalse(redissonClient.isShuttingDown());
    }

    @Test
    void test_redissonClient_connection() {
        String testKey = "test:config:connection";
        RBucket<String> bucket = redissonClient.getBucket(testKey);

        // Test set
        bucket.set("test_value");

        // Test get
        String value = bucket.get();
        assertEquals("test_value", value);

        // Cleanup
        bucket.delete();
    }

    @Test
    void test_redissonProperties_loaded() {
        assertNotNull(redissonProperties);
        assertTrue(redissonProperties.isEnabled());
        assertNotNull(redissonProperties.getMode());
    }

    @Test
    void test_singleServerConfig() {
        if (redissonProperties.getMode() == RedissonProperties.RedisMode.SINGLE) {
            assertNotNull(redissonProperties.getSingle());
            assertNotNull(redissonProperties.getSingle().getAddress());
            assertTrue(redissonProperties.getSingle().getAddress().startsWith("redis://"));
            assertTrue(redissonProperties.getSingle().getDatabase() >= 0);
        }
    }

    @Test
    void test_connectionPoolSettings() {
        assertTrue(redissonProperties.getConnectionPoolSize() > 0);
        assertTrue(redissonProperties.getConnectionMinimumIdleSize() > 0);
        assertTrue(redissonProperties.getTimeout() > 0);
        assertTrue(redissonProperties.getConnectTimeout() > 0);
    }
}
