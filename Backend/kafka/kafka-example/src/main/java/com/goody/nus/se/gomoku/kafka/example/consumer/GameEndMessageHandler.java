package com.goody.nus.se.gomoku.kafka.example.consumer;

import com.goody.nus.se.gomoku.kafka.client.KafkaMessageHandler;
import com.goody.nus.se.gomoku.kafka.example.model.GameEndMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Game end message consumer handler
 * Processes game end messages from Kafka
 */
@Slf4j
@Component
public class GameEndMessageHandler extends KafkaMessageHandler<GameEndMessage> {

    /**
     * Register this handler as a Spring Cloud Stream consumer bean
     * Bean name matches the function definition in application.yml
     *
     * @return consumer function
     */
    @Bean
    public Consumer<GameEndMessage> gameEndConsumer() {
        return this;
    }

    /**
     * Process game end message
     *
     * @param message the game end message
     */
    @Override
    protected void handleMessage(GameEndMessage message) {
        log.info("===================================");
        log.info("Received game end message:");
        log.info("Message ID: {}", message.getMessageId());
        log.info("Game ID: {}", message.getGameId());
        log.info("Winner ID: {}", message.getWinnerId());
        log.info("Loser ID: {}", message.getLoserId());
        log.info("Timestamp: {}", message.getTimestamp());
        log.info("===================================");

        // Add your business logic here
        processGameEnd(message);
    }

    /**
     * Business logic for game end processing
     *
     * @param message game end message
     */
    private void processGameEnd(GameEndMessage message) {
        log.info("Processing game end for game: {}", message.getGameId());
        log.info("Winner: {}, Loser: {}", message.getWinnerId(), message.getLoserId());

        // Example business logic:
        // - Update player rankings
        // - Save game result to database
        // - Send notifications to players
        // - Update statistics
    }

    /**
     * Custom error handling for game end messages
     *
     * @param message the message that caused the error
     * @param error   the exception
     */
    @Override
    protected void handleError(GameEndMessage message, Exception error) {
        log.error("Failed to process game end message - Game ID: {}, Error: {}",
                message.getGameId(), error.getMessage());

        // Custom error handling:
        // - Send to Dead Letter Queue
        // - Alert monitoring system
        // - Retry with backoff
    }
}
