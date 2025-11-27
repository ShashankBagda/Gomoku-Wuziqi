package com.goody.nus.se.gomoku.gomoku.model;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a game action performed by a player or system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAction {
    /**
     * Action type
     */
    private ActionType type;

    /**
     * Player who performed the action (null for system actions)
     */
    private Long playerId;

    /**
     * Stone position (only for MOVE action)
     */
    private Position position;

    /**
     * Stone color (for frontend convenience)
     */
    private PlayerColor color;

    /**
     * Action timestamp (critical for history tracking and timeout detection)
     */
    private Long timestamp;
}
