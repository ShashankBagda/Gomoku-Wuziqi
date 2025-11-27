package com.goody.nus.se.gomoku.gomoku.service.impl;

import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameHistoryRepository;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Game history service implementation
 *
 * <p>Manages the persistence and retrieval of historical game data.
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
public class GameHistoryServiceImpl implements IGameHistoryService {

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    /**
     * Archive a finished game to history
     *
     * <p>Creates a complete snapshot of the game state for future reference.
     * This method is transactional to ensure data integrity.
     *
     * @param gameDoc The finished game document
     * @param gameNumber The sequential game number in this room
     * @param endReason Human-readable reason for game end
     * @return The saved GameHistoryDocument
     */
    @Override
    public GameHistoryDocument archiveGame(GameDocument gameDoc, Integer gameNumber, String endReason) {
        log.info("[GameHistory] Archiving game: roomId={}, gameNumber={}, endReason={}",
                gameDoc.getRoomId(), gameNumber, endReason);

        // Create history document from current game state
        GameHistoryDocument historyDoc = GameHistoryDocument.fromGameDocument(gameDoc, gameNumber, endReason);

        // Save to MongoDB
        GameHistoryDocument saved = gameHistoryRepository.save(historyDoc);

        log.info("[GameHistory] Game archived successfully: roomId={}, gameNumber={}, historyId={}",
                gameDoc.getRoomId(), gameNumber, saved.getId());

        return saved;
    }

    /**
     * Get all game history for a room
     *
     * @param roomId The room ID
     * @return List of game history records, ordered by game number
     */
    @Override
    public List<GameHistoryDocument> getHistoryByRoomId(Long roomId) {
        log.debug("[GameHistory] Querying history for room: {}", roomId);
        return gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(roomId);
    }

    /**
     * Get a specific game from room history
     *
     * @param roomId The room ID
     * @param gameNumber The game number
     * @return Optional containing the game history, or empty if not found
     */
    @Override
    public Optional<GameHistoryDocument> getGameByRoomIdAndNumber(Long roomId, Integer gameNumber) {
        log.debug("[GameHistory] Querying specific game: roomId={}, gameNumber={}", roomId, gameNumber);
        return gameHistoryRepository.findByRoomIdAndGameNumber(roomId, gameNumber);
    }

    /**
     * Count total games played in a room
     *
     * @param roomId The room ID
     * @return Total number of games
     */
    @Override
    public long countGamesByRoomId(Long roomId) {
        long count = gameHistoryRepository.countByRoomId(roomId);
        log.debug("[GameHistory] Game count for room {}: {}", roomId, count);
        return count;
    }

    /**
     * Get all games where a player participated
     *
     * @param playerId The player ID
     * @return List of games where player was either black or white
     */
    @Override
    public List<GameHistoryDocument> getGamesByPlayerId(Long playerId) {
        log.debug("[GameHistory] Querying games for player: {}", playerId);
        // MongoDB query needs same parameter twice for OR condition
        return gameHistoryRepository.findByBlackPlayerIdOrWhitePlayerId(playerId, playerId);
    }

    /**
     * Get all games won by a player
     *
     * @param playerId The player ID
     * @return List of games won by this player
     */
    @Override
    public List<GameHistoryDocument> getWinsByPlayerId(Long playerId) {
        log.debug("[GameHistory] Querying wins for player: {}", playerId);
        return gameHistoryRepository.findByWinnerId(playerId);
    }

    /**
     * Delete all game history for a room
     *
     * @param roomId The room ID
     * @return Number of deleted records
     */
    @Override
    public long deleteHistoryByRoomId(Long roomId) {
        log.warn("[GameHistory] Deleting all history for room: {}", roomId);
        long deleted = gameHistoryRepository.deleteByRoomId(roomId);
        log.info("[GameHistory] Deleted {} history records for room {}", deleted, roomId);
        return deleted;
    }
}
