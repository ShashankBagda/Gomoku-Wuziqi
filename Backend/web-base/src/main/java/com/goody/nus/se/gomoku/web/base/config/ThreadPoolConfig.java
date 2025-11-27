package com.goody.nus.se.gomoku.web.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Thread Pool Configuration
 *
 * @author Haotian
 * @version 1.0, 2025/10/2
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("bizThreadPool")
    public Executor bizThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core pool size
        executor.setCorePoolSize(10);
        // Maximum pool size
        executor.setMaxPoolSize(20);
        // Queue capacity
        executor.setQueueCapacity(200);
        // Thread name prefix
        executor.setThreadNamePrefix("biz-thread-");
        // Keep alive time (seconds)
        executor.setKeepAliveSeconds(60);
        // Rejection policy: CallerRunsPolicy - runs in caller's thread when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // Await termination timeout (seconds)
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
