package com.goody.nus.se.gomoku.redis.service;

import com.goody.nus.se.gomoku.redis.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisService} test
 *
 * @author Haotian
 * @version 1.0, 2025/10/5
 */
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisServiceTest {

    private static final String TEST_KEY = "test:key";
    private static final String TEST_HASH_KEY = "test:hash";
    private static final String TEST_LIST_KEY = "test:list";
    private static final String TEST_SET_KEY = "test:set";
    private static final String TEST_SORTED_SET_KEY = "test:zset";
    @Autowired
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        // Clean up test keys before each test
        redisService.delete(Arrays.asList(
                TEST_KEY,
                TEST_HASH_KEY,
                TEST_LIST_KEY,
                TEST_SET_KEY,
                TEST_SORTED_SET_KEY
        ));
    }

    @AfterEach
    void tearDown() {
        // Clean up test keys after each test
        redisService.delete(Arrays.asList(
                TEST_KEY,
                TEST_HASH_KEY,
                TEST_LIST_KEY,
                TEST_SET_KEY,
                TEST_SORTED_SET_KEY
        ));
    }

    // ==================== String Operations Tests ====================

    @Test
    @Order(1)
    void test_set_and_get() {
        String value = "test_value";
        redisService.set(TEST_KEY, value);

        String result = redisService.get(TEST_KEY);
        assertEquals(value, result);
    }

    @Test
    @Order(2)
    void test_set_with_expiration() throws InterruptedException {
        String value = "expiring_value";
        redisService.set(TEST_KEY, value, 1, TimeUnit.SECONDS);

        String result = redisService.get(TEST_KEY);
        assertEquals(value, result);

        // Wait for expiration
        Thread.sleep(1100);
        String expiredResult = redisService.get(TEST_KEY);
        assertNull(expiredResult);
    }

    @Test
    @Order(3)
    void test_set_with_duration() throws InterruptedException {
        String value = "duration_value";
        redisService.set(TEST_KEY, value, Duration.ofSeconds(1));

        String result = redisService.get(TEST_KEY);
        assertEquals(value, result);

        Thread.sleep(1100);
        String expiredResult = redisService.get(TEST_KEY);
        assertNull(expiredResult);
    }

    @Test
    @Order(4)
    void test_delete() {
        redisService.set(TEST_KEY, "value_to_delete");
        assertTrue(redisService.exists(TEST_KEY));

        boolean deleted = redisService.delete(TEST_KEY);
        assertTrue(deleted);
        assertFalse(redisService.exists(TEST_KEY));
    }

    @Test
    @Order(5)
    void test_delete_multiple() {
        redisService.set(TEST_KEY + "1", "value1");
        redisService.set(TEST_KEY + "2", "value2");
        redisService.set(TEST_KEY + "3", "value3");

        long deletedCount = redisService.delete(Arrays.asList(
                TEST_KEY + "1",
                TEST_KEY + "2",
                TEST_KEY + "3"
        ));
        assertEquals(3, deletedCount);
    }

    @Test
    @Order(6)
    void test_exists() {
        assertFalse(redisService.exists(TEST_KEY));

        redisService.set(TEST_KEY, "value");
        assertTrue(redisService.exists(TEST_KEY));
    }

    @Test
    @Order(7)
    void test_expire() throws InterruptedException {
        redisService.set(TEST_KEY, "value");
        boolean expired = redisService.expire(TEST_KEY, 1, TimeUnit.SECONDS);
        assertTrue(expired);

        Thread.sleep(1100);
        assertFalse(redisService.exists(TEST_KEY));
    }

    // ==================== Hash Operations Tests ====================

    @Test
    @Order(10)
    void test_hSet_and_hGet() {
        String field = "field1";
        String value = "value1";

        redisService.hSet(TEST_HASH_KEY, field, value);
        String result = redisService.hGet(TEST_HASH_KEY, field);
        assertEquals(value, result);
    }

    @Test
    @Order(11)
    void test_hSetAll_and_hGetAll() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("field1", "value1");
        testMap.put("field2", "value2");
        testMap.put("field3", "value3");

        redisService.hSetAll(TEST_HASH_KEY, testMap);
        Map<String, String> result = redisService.hGetAll(TEST_HASH_KEY);

        assertEquals(3, result.size());
        assertEquals("value1", result.get("field1"));
        assertEquals("value2", result.get("field2"));
        assertEquals("value3", result.get("field3"));
    }

    @Test
    @Order(12)
    void test_hDelete() {
        redisService.hSet(TEST_HASH_KEY, "field1", "value1");
        redisService.hSet(TEST_HASH_KEY, "field2", "value2");

        long deleted = redisService.hDelete(TEST_HASH_KEY, "field1");
        assertEquals(1, deleted);
        assertFalse(redisService.hExists(TEST_HASH_KEY, "field1"));
        assertTrue(redisService.hExists(TEST_HASH_KEY, "field2"));
    }

    @Test
    @Order(13)
    void test_hExists() {
        assertFalse(redisService.hExists(TEST_HASH_KEY, "field1"));

        redisService.hSet(TEST_HASH_KEY, "field1", "value1");
        assertTrue(redisService.hExists(TEST_HASH_KEY, "field1"));
    }

    // ==================== List Operations Tests ====================

    @Test
    @Order(20)
    void test_lPush() {
        boolean result = redisService.lPush(TEST_LIST_KEY, "value1");
        assertTrue(result);

        List<String> list = redisService.lRange(TEST_LIST_KEY, 0, -1);
        assertEquals(1, list.size());
        assertEquals("value1", list.get(0));
    }

    @Test
    @Order(21)
    void test_lPushAll() {
        List<String> values = Arrays.asList("value1", "value2", "value3");
        boolean result = redisService.lPushAll(TEST_LIST_KEY, values);
        assertTrue(result);

        List<String> list = redisService.lRange(TEST_LIST_KEY, 0, -1);
        assertEquals(3, list.size());
    }

    @Test
    @Order(22)
    void test_lRange() {
        redisService.lPushAll(TEST_LIST_KEY, Arrays.asList("v1", "v2", "v3", "v4", "v5"));

        List<String> range = redisService.lRange(TEST_LIST_KEY, 0, 2);
        assertEquals(3, range.size());
        assertEquals("v1", range.get(0));
        assertEquals("v2", range.get(1));
        assertEquals("v3", range.get(2));
    }

    @Test
    @Order(23)
    void test_lSize() {
        redisService.lPushAll(TEST_LIST_KEY, Arrays.asList("v1", "v2", "v3"));

        int size = redisService.lSize(TEST_LIST_KEY);
        assertEquals(3, size);
    }

    // ==================== Set Operations Tests ====================

    @Test
    @Order(30)
    void test_sAdd() {
        boolean result = redisService.sAdd(TEST_SET_KEY, "value1");
        assertTrue(result);

        // Adding duplicate should return false
        boolean duplicate = redisService.sAdd(TEST_SET_KEY, "value1");
        assertFalse(duplicate);
    }

    @Test
    @Order(31)
    void test_sAddAll() {
        List<String> values = Arrays.asList("v1", "v2", "v3");
        boolean result = redisService.sAddAll(TEST_SET_KEY, values);
        assertTrue(result);

        Set<String> members = redisService.sMembers(TEST_SET_KEY);
        assertEquals(3, members.size());
    }

    @Test
    @Order(32)
    void test_sMembers() {
        redisService.sAddAll(TEST_SET_KEY, Arrays.asList("v1", "v2", "v3"));

        Set<String> members = redisService.sMembers(TEST_SET_KEY);
        assertEquals(3, members.size());
        assertTrue(members.contains("v1"));
        assertTrue(members.contains("v2"));
        assertTrue(members.contains("v3"));
    }

    @Test
    @Order(33)
    void test_sIsMember() {
        redisService.sAdd(TEST_SET_KEY, "value1");

        assertTrue(redisService.sIsMember(TEST_SET_KEY, "value1"));
        assertFalse(redisService.sIsMember(TEST_SET_KEY, "value2"));
    }

    @Test
    @Order(34)
    void test_sRemove() {
        redisService.sAdd(TEST_SET_KEY, "value1");
        assertTrue(redisService.sIsMember(TEST_SET_KEY, "value1"));

        boolean removed = redisService.sRemove(TEST_SET_KEY, "value1");
        assertTrue(removed);
        assertFalse(redisService.sIsMember(TEST_SET_KEY, "value1"));
    }

    // ==================== Sorted Set Operations Tests ====================

    @Test
    @Order(40)
    void test_zAdd() {
        boolean result = redisService.zAdd(TEST_SORTED_SET_KEY, 1.0, "value1");
        assertTrue(result);

        Collection<String> range = redisService.zRange(TEST_SORTED_SET_KEY, 0, -1);
        assertEquals(1, range.size());
    }

    @Test
    @Order(41)
    void test_zRange() {
        redisService.zAdd(TEST_SORTED_SET_KEY, 1.0, "v1");
        redisService.zAdd(TEST_SORTED_SET_KEY, 2.0, "v2");
        redisService.zAdd(TEST_SORTED_SET_KEY, 3.0, "v3");

        Collection<String> range = redisService.zRange(TEST_SORTED_SET_KEY, 0, 1);
        assertEquals(2, range.size());

        List<String> list = new ArrayList<>(range);
        assertEquals("v1", list.get(0));
        assertEquals("v2", list.get(1));
    }

    @Test
    @Order(42)
    void test_zRangeByScore() {
        redisService.zAdd(TEST_SORTED_SET_KEY, 1.0, "v1");
        redisService.zAdd(TEST_SORTED_SET_KEY, 2.0, "v2");
        redisService.zAdd(TEST_SORTED_SET_KEY, 3.0, "v3");
        redisService.zAdd(TEST_SORTED_SET_KEY, 4.0, "v4");

        Collection<String> range = redisService.zRangeByScore(TEST_SORTED_SET_KEY, 2.0, 3.0);
        assertEquals(2, range.size());

        List<String> list = new ArrayList<>(range);
        assertTrue(list.contains("v2"));
        assertTrue(list.contains("v3"));
    }

    // ==================== Lock Operations Tests ====================

    @Test
    @Order(50)
    void test_tryLock() {
        String lockKey = "test:lock";

        boolean locked = redisService.tryLock(lockKey, 100, 1000, TimeUnit.MILLISECONDS);
        assertTrue(locked);

        // Try to lock again, should true, reentry lock is supported
        boolean lockedAgain = redisService.tryLock(lockKey, 100, 1000, TimeUnit.MILLISECONDS);
        assertTrue(lockedAgain);

        // Unlock and try again
        redisService.unlock(lockKey);
        boolean lockedAfterUnlock = redisService.tryLock(lockKey, 100, 1000, TimeUnit.MILLISECONDS);
        assertTrue(lockedAfterUnlock);

        redisService.unlock(lockKey);
        redisService.delete(lockKey);
    }

    @Test
    @Order(51)
    void test_unlock() {
        String lockKey = "test:lock2";

        redisService.tryLock(lockKey, 100, 1000, TimeUnit.MILLISECONDS);
        redisService.unlock(lockKey);

        // After unlock, should be able to lock again
        boolean locked = redisService.tryLock(lockKey, 100, 1000, TimeUnit.MILLISECONDS);
        assertTrue(locked);

        redisService.unlock(lockKey);
        redisService.delete(lockKey);
    }

    // ==================== Utility Operations Tests ====================

    @Test
    @Order(60)
    void test_keys() {
        redisService.set("test:pattern:1", "value1");
        redisService.set("test:pattern:2", "value2");
        redisService.set("test:pattern:3", "value3");

        Iterable<String> keys = redisService.keys("test:pattern:*");
        assertNotNull(keys);

        List<String> keyList = new ArrayList<>();
        keys.forEach(keyList::add);
        assertEquals(3, keyList.size());

        // Cleanup
        redisService.delete(keyList);
    }

    @Test
    @Order(61)
    void test_getClient() {
        assertNotNull(redisService.getClient());
    }
}
