package com.goody.nus.se.gomoku.kafka.client;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Base Kafka message consumer handler
 * Provides generic message handling interface
 *
 * @param <T> message type
 */
@Slf4j
public abstract class KafkaMessageHandler<T> implements Consumer<T> {

    /**
     * Handle incoming message from Kafka
     *
     * @param message the incoming message
     */
    @Override
    public void accept(T message) {
        try {
            log.debug("Received message: {}", message);
            handleMessage(message);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
            handleError(message, e);
        }
    }

    /**
     * Process the message (to be implemented by subclasses)
     *
     * @param message the message to process
     */
    protected abstract void handleMessage(T message);

    /**
     * Handle processing errors (can be overridden by subclasses)
     *
     * @param message the message that caused the error
     * @param error   the exception
     */
    protected void handleError(T message, Exception error) {
        // Default error handling: log only
        // Subclasses can override to add custom error handling (e.g., send to DLQ)
        log.error("Default error handler - message: {}, error: {}", message, error.getMessage());
    }
}
