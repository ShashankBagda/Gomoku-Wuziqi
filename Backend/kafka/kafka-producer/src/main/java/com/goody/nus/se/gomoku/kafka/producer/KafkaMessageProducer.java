package com.goody.nus.se.gomoku.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * Generic Kafka message producer service
 * Uses Spring Cloud Stream's StreamBridge to send messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final StreamBridge streamBridge;

    /**
     * Send message to Kafka topic
     *
     * @param bindingName binding name (e.g., "gameEnd-out-0")
     * @param message     message object to send
     * @param <T>         message type
     * @return true if sent successfully, false otherwise
     */
    public <T> boolean send(String bindingName, T message) {
        try {
            log.debug("Sending message to Kafka binding {}: {}", bindingName, message);
            boolean result = streamBridge.send(bindingName, message);
            log.debug("Message sent to {} successfully: {}", bindingName, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to send message to Kafka binding: {}", bindingName, e);
            return false;
        }
    }

    /**
     * Send message to Kafka topic with default binding name pattern
     *
     * @param topicName topic name (will be converted to binding name)
     * @param message   message object to send
     * @param <T>       message type
     * @return true if sent successfully, false otherwise
     */
    public <T> boolean sendToTopic(String topicName, T message) {
        String bindingName = topicName + "-out-0";
        return send(bindingName, message);
    }
}
