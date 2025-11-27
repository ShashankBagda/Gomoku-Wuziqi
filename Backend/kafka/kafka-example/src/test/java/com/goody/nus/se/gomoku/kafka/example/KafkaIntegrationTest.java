package com.goody.nus.se.gomoku.kafka.example;

import com.goody.nus.se.gomoku.kafka.example.model.GameEndMessage;
import com.goody.nus.se.gomoku.kafka.producer.KafkaMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kafka integration test
 * Tests producer and consumer functionality
 * <p>
 * Prerequisites:
 * - External Kafka must be accessible at kafka.goodyhao.me:8201
 */
@Slf4j
@SpringBootTest(classes = KafkaTestApplication.class)
@Disabled
class KafkaIntegrationTest {

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    /**
     * Test sending game end message
     */
    @Test
    void testSendGameEndMessage() throws InterruptedException {
        log.info("=== Starting Kafka Integration Test ===");

        // Arrange
        GameEndMessage message = GameEndMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .gameId("game-test-001")
                .winnerId("player-A")
                .loserId("player-B")
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Prepared test message: {}", message);

        // Act
        log.info("Attempting to send message to Kafka...");
        boolean result = kafkaMessageProducer.send("gameEnd-out-0", message);

        // Assert
        assertTrue(result, "Message should be sent successfully");
        log.info("Message sent successfully, result: {}", result);

        // Wait for consumer to process the message
        log.info("Waiting for consumer to process the message...");
        TimeUnit.SECONDS.sleep(3);

        log.info("=== Test Completed Successfully ===");
    }

    /**
     * Test sending multiple messages
     */
    @Test
    void testSendMultipleMessages() throws InterruptedException {
        log.info("Testing multiple message sending");

        for (int i = 1; i <= 5; i++) {
            GameEndMessage message = GameEndMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .gameId("game-test-" + String.format("%03d", i))
                    .winnerId("player-winner-" + i)
                    .loserId("player-loser-" + i)
                    .timestamp(System.currentTimeMillis())
                    .build();

            boolean result = kafkaMessageProducer.send("gameEnd-out-0", message);
            assertTrue(result, "Message " + i + " should be sent successfully");

            log.info("Sent message {}: {}", i, message.getMessageId());
        }

        // Wait for consumers to process all messages
        TimeUnit.SECONDS.sleep(3);

        log.info("Multiple messages test completed");
    }
}
