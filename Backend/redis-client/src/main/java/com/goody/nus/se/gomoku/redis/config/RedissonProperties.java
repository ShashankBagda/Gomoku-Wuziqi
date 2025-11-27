package com.goody.nus.se.gomoku.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redisson Configuration Properties
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    /**
     * Enable Redis (default: true)
     */
    private boolean enabled = true;

    /**
     * Redis mode: SINGLE, CLUSTER, SENTINEL
     */
    private RedisMode mode = RedisMode.SINGLE;

    /**
     * Redis password
     */
    private String password;

    /**
     * Connection pool size
     */
    private int connectionPoolSize = 64;

    /**
     * Minimum idle connections
     */
    private int connectionMinimumIdleSize = 10;

    /**
     * Command timeout in milliseconds
     */
    private int timeout = 3000;

    /**
     * Connection timeout in milliseconds
     */
    private int connectTimeout = 10000;

    /**
     * Single server configuration
     */
    private SingleServerConfig single = new SingleServerConfig();

    /**
     * Cluster configuration
     */
    private ClusterConfig cluster = new ClusterConfig();

    /**
     * Sentinel configuration
     */
    private SentinelConfig sentinel = new SentinelConfig();

    public enum RedisMode {
        SINGLE,
        CLUSTER,
        SENTINEL
    }

    @Data
    public static class SingleServerConfig {
        /**
         * Redis address (e.g., redis://127.0.0.1:6379)
         */
        private String address = "redis://127.0.0.1:6379";

        /**
         * Database index
         */
        private int database = 0;
    }

    @Data
    public static class ClusterConfig {
        /**
         * Cluster node addresses
         */
        private List<String> nodeAddresses;
    }

    @Data
    public static class SentinelConfig {
        /**
         * Master name
         */
        private String masterName;

        /**
         * Sentinel addresses
         */
        private List<String> sentinelAddresses;

        /**
         * Database index
         */
        private int database = 0;
    }
}
