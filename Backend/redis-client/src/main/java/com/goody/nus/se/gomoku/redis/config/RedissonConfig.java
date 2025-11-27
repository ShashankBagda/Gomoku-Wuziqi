package com.goody.nus.se.gomoku.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson Configuration
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();

        // Configure based on mode: single, cluster, or sentinel
        switch (properties.getMode()) {
            case SINGLE:
                config.useSingleServer()
                        .setAddress(properties.getSingle().getAddress())
                        .setDatabase(properties.getSingle().getDatabase())
                        .setPassword(properties.getPassword())
                        .setConnectionPoolSize(properties.getConnectionPoolSize())
                        .setConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize())
                        .setTimeout(properties.getTimeout())
                        .setConnectTimeout(properties.getConnectTimeout());
                break;

            case CLUSTER:
                config.useClusterServers()
                        .addNodeAddress(properties.getCluster().getNodeAddresses().toArray(new String[0]))
                        .setPassword(properties.getPassword())
                        .setMasterConnectionPoolSize(properties.getConnectionPoolSize())
                        .setMasterConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize())
                        .setTimeout(properties.getTimeout())
                        .setConnectTimeout(properties.getConnectTimeout());
                break;

            case SENTINEL:
                config.useSentinelServers()
                        .setMasterName(properties.getSentinel().getMasterName())
                        .addSentinelAddress(properties.getSentinel().getSentinelAddresses().toArray(new String[0]))
                        .setDatabase(properties.getSentinel().getDatabase())
                        .setPassword(properties.getPassword())
                        .setMasterConnectionPoolSize(properties.getConnectionPoolSize())
                        .setMasterConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize())
                        .setTimeout(properties.getTimeout())
                        .setConnectTimeout(properties.getConnectTimeout());
                break;

            default:
                throw new IllegalArgumentException("Unsupported Redis mode: " + properties.getMode());
        }

        return Redisson.create(config);
    }
}
