package com.goody.nus.se.gomoku.gomoku.api.response;

import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for game state query and action execution
 * This unified structure mirrors GameDocument but without MongoDB-specific annotations
 * Provides a clean API response that matches the game document structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {

    /**
     * Room ID
     */
    private Long roomId;

    /**
     * Black player ID
     */
    private Long blackPlayerId;

    /**
     * White player ID
     */
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
     * Current game state snapshot
     */
    private GameStateSnapshot currentState;

    /**
     * Last action performed
     */
    private GameAction lastAction;

    /**
     * Action history (for replay functionality)
     */
    private List<GameAction> actionHistory;

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
     */
    private PlayerColor drawProposerColor;

    /**
     * Game mode type (RANKED, CASUAL, PRIVATE)
     */
    private String modeType;
}
