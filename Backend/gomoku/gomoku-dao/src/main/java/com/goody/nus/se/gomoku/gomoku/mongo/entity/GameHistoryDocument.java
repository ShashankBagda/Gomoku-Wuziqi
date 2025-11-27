package com.goody.nus.se.gomoku.gomoku.mongo.entity;

import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB document for game history
 *
 * <p>This document stores the complete history of finished games in a room.
 * When players restart a game, the current game state is archived here
 * before resetting the active GameDocument.
 *
 * <p>Design principles:
 * <ul>
 *   <li>Separation of concerns: Active games (GameDocument) vs historical games (GameHistoryDocument)</li>
 *   <li>Complete record: Stores full action history and final state for replay/analysis</li>
 *   <li>Queryable: Indexed by roomId and gameNumber for efficient retrieval</li>
 * </ul>
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "game_history")
@CompoundIndexes({
        @CompoundIndex(name = "room_game_idx", def = "{'roomId': 1, 'gameNumber': 1}", unique = true)
})
public class GameHistoryDocument {

    /**
     * Auto-generated document ID
     */
    @Id
    private String id;

    /**
     * Room ID (links to GameRoom in MySQL)
     */
    private Long roomId;

    /**
     * Game number in this room (1st game, 2nd game, etc.)
     */
    private Integer gameNumber;

    /**
     * Black player ID
     */
    private Long blackPlayerId;

    /**
     * White player ID
     */
    private Long whitePlayerId;

    /**
     * Final state snapshot when game ended
     */
    private GameStateSnapshot finalState;

    /**
     * Complete action history for replay
     */
    private List<GameAction> actionHistory;

    /**
     * Game start time (when both players readied up)
     */
    private Long startTime;

    /**
     * Game end time (when game reached FINISHED status)
     */
    private Long endTime;

    /**
     * Winner: null=draw/ongoing, playerId of winner
     * Derived from finalState.winner and player colors
     */
    private Long winnerId;

    /**
     * Total number of moves in this game
     */
    private Integer totalMoves;

    /**
     * Reason for game end (e.g., "WIN", "SURRENDER", "DRAW", "TIMEOUT")
     */
    private String endReason;

    /**
     * Create a history record from a finished GameDocument
     *
     * <p>This factory method extracts all necessary information from an active
     * GameDocument and prepares it for archival storage.
     *
     * @param gameDoc The finished game document
     * @param gameNumber The sequential game number in this room
     * @param endReason Human-readable reason for game end
     * @return A new GameHistoryDocument ready to be saved
     */
    public static GameHistoryDocument fromGameDocument(GameDocument gameDoc, Integer gameNumber, String endReason) {
        Long winnerId = determineWinnerId(gameDoc);

        return GameHistoryDocument.builder()
                .roomId(gameDoc.getRoomId())
                .gameNumber(gameNumber)
                .blackPlayerId(gameDoc.getBlackPlayerId())
                .whitePlayerId(gameDoc.getWhitePlayerId())
                .finalState(gameDoc.getCurrentState())
                .actionHistory(gameDoc.getActionHistory())
                .startTime(gameDoc.getCreateTime())
                .endTime(System.currentTimeMillis())
                .winnerId(winnerId)
                .totalMoves(gameDoc.getCurrentState() != null ? gameDoc.getCurrentState().getTotalMoves() : 0)
                .endReason(endReason)
                .build();
    }

    /**
     * Determine the winner's player ID from game state
     *
     * @param gameDoc The game document
     * @return Winner's player ID, or null if draw
     */
    private static Long determineWinnerId(GameDocument gameDoc) {
        GameStateSnapshot state = gameDoc.getCurrentState();
        if (state == null || state.getWinner() == null) {
            return null;
        }

        // winner: -1=ongoing, 0=draw, 1=black, 2=white
        Integer winner = state.getWinner();
        if (winner == null || winner == -1 || winner == 0) {
            return null; // No winner (ongoing or draw)
        } else if (winner == 1) {
            return gameDoc.getBlackPlayerId();
        } else if (winner == 2) {
            return gameDoc.getWhitePlayerId();
        }

        return null;
    }

    /**
     * Get game duration in milliseconds
     *
     * @return Duration of the game, or 0 if times not set
     */
    public Long getDuration() {
        if (startTime != null && endTime != null) {
            return endTime - startTime;
        }
        return 0L;
    }
}
