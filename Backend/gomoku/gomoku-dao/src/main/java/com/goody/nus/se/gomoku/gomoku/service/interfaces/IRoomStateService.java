package com.goody.nus.se.gomoku.gomoku.service.interfaces;

import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;

/**
 * Room state management service interface
 *
 * <p>This service is responsible for initializing and managing room state
 * across different storage layers (MySQL and MongoDB) to maintain consistency.
 *
 * <p>Design principles:
 * <ul>
 *   <li>High cohesion: All room state initialization logic is centralized here</li>
 *   <li>Low coupling: Match and join modules interact through this interface</li>
 *   <li>Reusability: Both match and join flows use the same initialization logic</li>
 * </ul>
 *
 * @author Generated
 * @version 1.0
 * @since 1.0.0
 */
public interface IRoomStateService {

    /**
     * Initialize game state in MongoDB when two players are successfully matched
     *
     * <p>This method creates a new GameDocument with:
     * <ul>
     *   <li>Random color assignment (black/white) for both players</li>
     *   <li>Initial game state (WAITING status)</li>
     *   <li>Empty board and action history</li>
     *   <li>Ready flags set to false</li>
     *   <li>Game mode type (RANKED, CASUAL, PRIVATE)</li>
     * </ul>
     *
     * <p>This ensures that when a client queries room state via GameService,
     * the MongoDB document already exists and is properly initialized.
     *
     * @param roomId The room ID (primary key for both MySQL GameRoom and MongoDB GameDocument)
     * @param player1Id First player's user ID
     * @param player2Id Second player's user ID
     * @param modeType Game mode type (RANKED, CASUAL, PRIVATE)
     * @return The initialized GameDocument
     * @throws com.goody.nus.se.gomoku.common.exception.BizException if initialization fails
     */
    GameDocument initializeGameState(Long roomId, Long player1Id, Long player2Id, String modeType);
}
