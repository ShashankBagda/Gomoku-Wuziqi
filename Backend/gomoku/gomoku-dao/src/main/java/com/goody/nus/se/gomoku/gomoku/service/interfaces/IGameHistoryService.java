package com.goody.nus.se.gomoku.gomoku.service.interfaces;

import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;

import java.util.List;
import java.util.Optional;

/**
 * Game history service interface
 *
 * <p>This service manages the archival and retrieval of finished games.
 * When players restart a game, the current game state is saved as history
 * before resetting the active game.
 *
 * <p>Design principles:
 * <ul>
 *   <li>Single responsibility: Focused on historical game data management</li>
 *   <li>Reusability: Can be used by both game service and analytics/stats features</li>
 *   <li>Data integrity: Ensures complete and consistent historical records</li>
 * </ul>
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
public interface IGameHistoryService {

    /**
     * Archive a finished game to history
     *
     * <p>This method creates a historical record from a completed GameDocument.
     * It should be called before resetting the game for a restart.
     *
     * @param gameDoc The finished game document
     * @param gameNumber The sequential game number in this room (1st, 2nd, etc.)
     * @param endReason Human-readable reason for game end (e.g., "WIN", "SURRENDER")
     * @return The saved GameHistoryDocument
     */
    GameHistoryDocument archiveGame(GameDocument gameDoc, Integer gameNumber, String endReason);

    /**
     * Get all game history for a room
     *
     * @param roomId The room ID
     * @return List of game history records, ordered by game number
     */
    List<GameHistoryDocument> getHistoryByRoomId(Long roomId);

    /**
     * Get a specific game from room history
     *
     * @param roomId The room ID
     * @param gameNumber The game number
     * @return Optional containing the game history, or empty if not found
     */
    Optional<GameHistoryDocument> getGameByRoomIdAndNumber(Long roomId, Integer gameNumber);

    /**
     * Count total games played in a room
     *
     * @param roomId The room ID
     * @return Total number of games
     */
    long countGamesByRoomId(Long roomId);

    /**
     * Get all games where a player participated
     *
     * @param playerId The player ID
     * @return List of games where player was either black or white
     */
    List<GameHistoryDocument> getGamesByPlayerId(Long playerId);

    /**
     * Get all games won by a player
     *
     * @param playerId The player ID
     * @return List of games won by this player
     */
    List<GameHistoryDocument> getWinsByPlayerId(Long playerId);

    /**
     * Delete all game history for a room
     *
     * <p>Use with caution - this permanently removes all historical data
     *
     * @param roomId The room ID
     * @return Number of deleted records
     */
    long deleteHistoryByRoomId(Long roomId);
}
