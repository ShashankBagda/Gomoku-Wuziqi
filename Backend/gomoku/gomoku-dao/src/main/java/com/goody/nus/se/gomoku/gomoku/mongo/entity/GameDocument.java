package com.goody.nus.se.gomoku.gomoku.mongo.entity;

import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for game state
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class GameDocument {

    /**
     * Room ID as primary key
     */
    @Id
    private Long roomId;

    /**
     * Black player ID
     */
    @Nullable
    private Long blackPlayerId;

    /**
     * White player ID
     */
    @Nullable
    private Long whitePlayerId;

    /**
     * Black player ready status
     */
    private Boolean blackReady;

    /**
     * White player ready status
     */
    private Boolean whiteReady;

    /**
     * Current state snapshot
     */
    private GameStateSnapshot currentState;

    /**
     * Last action (for frontend diff detection)
     */
    private GameAction lastAction;

    /**
     * Action history (for replay functionality)
     */
    @Builder.Default
    private List<GameAction> actionHistory = new ArrayList<>();

    /**
     * Version for optimistic locking and state tracking
     */
    private Long version;

    /**
     * Game creation time
     */
    private Long createTime;

    /**
     * Last update time
     */
    private Long updateTime;

    /**
     * Game status
     */
    private GameStatus status;

    /**
     * Draw proposer color (null if no pending draw proposal)
     * Used to track who proposed a draw and validate responses
     */
    private PlayerColor drawProposerColor;

    /**
     * Undo proposer color (null if no pending undo proposal)
     * Used to track who proposed an undo and validate responses
     */
    private PlayerColor undoProposerColor;

    /**
     * Restart proposer color (null if no pending restart proposal)
     * Used to track who proposed a restart and validate responses
     */
    private PlayerColor restartProposerColor;

    /**
     * Game count - tracks how many games have been played in this room
     * Starts at 1 for the first game, increments on each restart
     */
    @Builder.Default
    private Integer gameCount = 1;

    /**
     * Game mode type (RANKED, CASUAL, PRIVATE)
     * Used for ranking settlement
     */
    private String modeType;

    /**
     * Create a new game document for a room
     */
    public static GameDocument createNewGameWithRandomBlack(Long roomId, Long playerId, String modeType) {
        long now = System.currentTimeMillis();
        final boolean isBlack = now % 2 == 0;
        return GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(isBlack ? playerId : null)
                .whitePlayerId(isBlack ? null : playerId)
                .blackReady(false)
                .whiteReady(false)
                .currentState(GameStateSnapshot.createEmpty())
                .lastAction(null)
                .actionHistory(new ArrayList<>())
                .version(0L)
                .createTime(now)
                .updateTime(now)
                .status(GameStatus.WAITING)
                .drawProposerColor(null)
                .undoProposerColor(null)
                .modeType(modeType)
                .build();
    }

    /**
     * Add action to history
     */
    public void addActionToHistory(GameAction action) {
        if (this.actionHistory == null) {
            this.actionHistory = new ArrayList<>();
        }
        this.actionHistory.add(action);
    }

    /**
     * Clear draw proposal
     */
    public void clearDrawProposal() {
        this.drawProposerColor = null;
    }

    /**
     * Check if there's a pending draw proposal
     */
    public boolean hasPendingDrawProposal() {
        return this.drawProposerColor != null;
    }

    /**
     * Clear undo proposal
     */
    public void clearUndoProposal() {
        this.undoProposerColor = null;
    }

    /**
     * Check if there's a pending undo proposal
     */
    public boolean hasPendingUndoProposal() {
        return this.undoProposerColor != null;
    }

    /**
     * Clear restart proposal
     */
    public void clearRestartProposal() {
        this.restartProposerColor = null;
    }

    /**
     * Check if there's a pending restart proposal
     */
    public boolean hasPendingRestartProposal() {
        return this.restartProposerColor != null;
    }

    /**
     * Reset game state for a new game (after archiving to history)
     *
     * <p>This method resets the game to initial state while:
     * <ul>
     *   <li>Keeping the same roomId and player IDs</li>
     *   <li>Swapping player colors for fairness</li>
     *   <li>Incrementing game count</li>
     *   <li>Clearing all game state and proposals</li>
     * </ul>
     *
     * <p>Call this AFTER archiving the current game to history.
     */
    public void resetForNewGame() {
        long now = System.currentTimeMillis();

        // Swap player colors for fairness
        Long tempBlackId = this.blackPlayerId;
        this.blackPlayerId = this.whitePlayerId;
        this.whitePlayerId = tempBlackId;

        // Reset game state
        this.currentState = GameStateSnapshot.createEmpty();
        this.lastAction = null;
        this.actionHistory = new ArrayList<>();

        // Reset ready status
        this.blackReady = false;
        this.whiteReady = false;

        // Reset game status
        this.status = GameStatus.WAITING;

        // Clear all proposals
        this.drawProposerColor = null;
        this.undoProposerColor = null;
        this.restartProposerColor = null;

        // Increment game count
        if (this.gameCount == null) {
            this.gameCount = 1;
        }
        this.gameCount++;

        // Update timestamps
        this.updateTime = now;
        // Note: We keep the original createTime, only update updateTime
    }
}
