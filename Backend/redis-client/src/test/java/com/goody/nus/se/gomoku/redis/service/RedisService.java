package com.goody.nus.se.gomoku.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Service using Redisson Client
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedissonClient redissonClient;

    // ==================== String Operations ====================

    /**
     * Set value
     */
    public <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * Set value with expiration time
     */
    public <T> void set(String key, T value, long time, TimeUnit timeUnit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, time, timeUnit);
    }

    /**
     * Set value with duration
     */
    public <T> void set(String key, T value, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Get value
     */
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * Delete key
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * Delete multiple keys
     */
    public long delete(Collection<String> keys) {
        return redissonClient.getKeys().delete(keys.toArray(new String[0]));
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * Set expiration time
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        return redissonClient.getBucket(key).expire(Duration.ofMillis(timeUnit.toMillis(time)));
    }

    // ==================== Hash Operations ====================

    /**
     * Get hash value
     */
    public <T> T hGet(String key, String field) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * Set hash value
     */
    public <T> void hSet(String key, String field, T value) {
        RMap<String, T> map = redissonClient.getMap(key);
        map.put(field, value);
    }

    /**
     * Get all hash entries
     */
    public <T> Map<String, T> hGetAll(String key) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * Set multiple hash values
     */
    public <T> void hSetAll(String key, Map<String, T> map) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        rMap.putAll(map);
    }

    /**
     * Delete hash field
     */
    public long hDelete(String key, String... fields) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.fastRemove(fields);
    }

    /**
     * Check if hash field exists
     */
    public boolean hExists(String key, String field) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.containsKey(field);
    }

    // ==================== List Operations ====================

    /**
     * Get list
     */
    public <T> RList<T> getList(String key) {
        return redissonClient.getList(key);
    }

    /**
     * Add to list (right push)
     */
    public <T> boolean lPush(String key, T value) {
        RList<T> list = redissonClient.getList(key);
        return list.add(value);
    }

    /**
     * Add multiple values to list
     */
    public <T> boolean lPushAll(String key, Collection<T> values) {
        RList<T> list = redissonClient.getList(key);
        return list.addAll(values);
    }

    /**
     * Get list range
     */
    public <T> List<T> lRange(String key, int fromIndex, int toIndex) {
        RList<T> list = redissonClient.getList(key);
        return list.range(fromIndex, toIndex);
    }

    /**
     * Get list size
     */
    public int lSize(String key) {
        RList<?> list = redissonClient.getList(key);
        return list.size();
    }

    // ==================== Set Operations ====================

    /**
     * Get set
     */
    public <T> RSet<T> getSet(String key) {
        return redissonClient.getSet(key);
    }

    /**
     * Add to set
     */
    public <T> boolean sAdd(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.add(value);
    }

    /**
     * Add multiple values to set
     */
    public <T> boolean sAddAll(String key, Collection<T> values) {
        RSet<T> set = redissonClient.getSet(key);
        return set.addAll(values);
    }

    /**
     * Get all set members
     */
    public <T> Set<T> sMembers(String key) {
        RSet<T> set = redissonClient.getSet(key);
        return set.readAll();
    }

    /**
     * Check if member exists in set
     */
    public <T> boolean sIsMember(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.contains(value);
    }

    /**
     * Remove from set
     */
    public <T> boolean sRemove(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.remove(value);
    }

    // ==================== Sorted Set Operations ====================

    /**
     * Get sorted set
     */
    public <T> RScoredSortedSet<T> getSortedSet(String key) {
        return redissonClient.getScoredSortedSet(key);
    }

    /**
     * Add to sorted set
     */
    public <T> boolean zAdd(String key, double score, T value) {
        RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.add(score, value);
    }

    /**
     * Get sorted set range
     */
    public <T> Collection<T> zRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.valueRange(startIndex, endIndex);
    }

    /**
     * Get sorted set range by score
     */
    public <T> Collection<T> zRangeByScore(String key, double startScore, double endScore) {
        RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.valueRange(startScore, true, endScore, true);
    }

    // ==================== Lock Operations ====================

    /**
     * Get lock
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * Try lock
     */
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Unlock
     */
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // ==================== Utility Operations ====================

    /**
     * Get keys by pattern
     */
    public Iterable<String> keys(String pattern) {
        return redissonClient.getKeys().getKeysByPattern(pattern);
    }

    /**
     * Get RedissonClient for advanced operations
     */
    public RedissonClient getClient() {
        return redissonClient;
    }
}
