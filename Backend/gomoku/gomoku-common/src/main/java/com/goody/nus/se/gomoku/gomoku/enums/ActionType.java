package com.goody.nus.se.gomoku.gomoku.enums;

/**
 * Game action types
 */
public enum ActionType {
    /**
     * Player marks ready (during WAITING phase)
     * When both players are ready, game automatically starts
     */
    READY,

    /**
     * Player places stone
     */
    MOVE,

    /**
     * Player surrenders
     */
    SURRENDER,

    /**
     * Player proposes a draw
     */
    DRAW,

    /**
     * Player agrees to the draw proposal
     */
    DRAW_AGREE,

    /**
     * Player disagrees with the draw proposal
     */
    DRAW_DISAGREE,

    /**
     * Player proposes to undo the last move
     */
    UNDO,

    /**
     * Player agrees to the undo proposal
     */
    UNDO_AGREE,

    /**
     * Player disagrees with the undo proposal
     */
    UNDO_DISAGREE,

    /**
     * Player proposes to restart the game (start a new game in same room)
     * Available only after game is finished
     */
    RESTART,

    /**
     * Player agrees to the restart proposal
     */
    RESTART_AGREE,

    /**
     * Player disagrees with the restart proposal
     */
    RESTART_DISAGREE,

    /**
     * Time runs out (system triggered)
     */
    TIMEOUT
}
