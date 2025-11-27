package com.goody.nus.se.gomoku.gomoku.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a position on the Gomoku board
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    /**
     * X coordinate (0-14)
     */
    private int x;

    /**
     * Y coordinate (0-14)
     */
    private int y;

    /**
     * Validate if position is within board bounds
     */
    public boolean isValid(int boardSize) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }
}
