package com.goody.nus.se.gomoku.kafka.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Game end message entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEndMessage {
    /**
     * Message unique identifier
     */
    private String messageId;

    /**
     * Game ID
     */
    private String gameId;

    /**
     * Winner player ID
     */
    private String winnerId;

    /**
     * Loser player ID
     */
    private String loserId;

    /**
     * Message timestamp
     */
    private Long timestamp;
}
