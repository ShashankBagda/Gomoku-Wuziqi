package com.goody.nus.se.gomoku.gomoku.enums;

/**
 * Game status
 */
public enum GameStatus {
    /**
     * Waiting for players to ready up
     */
    WAITING,

    /**
     * Game in progress
     */
    PLAYING,

    /**
     * Game ended (normal finish, surrender, or timeout)
     */
    FINISHED
}
