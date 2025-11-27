package com.goody.nus.se.gomoku.gomoku.mongo.repository;

import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for game history
 *
 * <p>Provides data access methods for querying historical games.
 * All queries are indexed for efficient retrieval.
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Repository
public interface GameHistoryRepository extends MongoRepository<GameHistoryDocument, String> {

    /**
     * Find all games for a specific room, ordered by game number
     *
     * @param roomId The room ID
     * @return List of game history records, ordered from oldest to newest
     */
    List<GameHistoryDocument> findByRoomIdOrderByGameNumberAsc(Long roomId);

    /**
     * Find a specific game in a room by game number
     *
     * @param roomId The room ID
     * @param gameNumber The sequential game number
     * @return Optional containing the game history, or empty if not found
     */
    Optional<GameHistoryDocument> findByRoomIdAndGameNumber(Long roomId, Integer gameNumber);

    /**
     * Count total games played in a room
     *
     * @param roomId The room ID
     * @return Total number of games played
     */
    long countByRoomId(Long roomId);

    /**
     * Find all games where a specific player participated
     *
     * @param playerId The player ID
     * @return List of game history records where player was either black or white
     */
    List<GameHistoryDocument> findByBlackPlayerIdOrWhitePlayerId(Long playerId, Long playerId2);

    /**
     * Find all games won by a specific player
     *
     * @param playerId The player ID
     * @return List of game history records won by this player
     */
    List<GameHistoryDocument> findByWinnerId(Long playerId);

    /**
     * Delete all game history for a room
     *
     * @param roomId The room ID
     * @return Number of deleted records
     */
    long deleteByRoomId(Long roomId);
}
