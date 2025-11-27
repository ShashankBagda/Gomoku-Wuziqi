package com.goody.nus.se.gomoku.gomoku.game.util;

import com.goody.nus.se.gomoku.gomoku.model.Position;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for checking win conditions in Gomoku
 * Checks if 5 or more stones in a row exist (horizontal, vertical, or diagonal)
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
public class WinConditionChecker {

    /**
     * Check if the last move resulted in a win
     *
     * @param board    the game board
     * @param position the last move position
     * @param color    the player color value (1 for BLACK, 2 for WHITE)
     * @return true if the move wins the game, false otherwise
     */
    public static boolean checkWin(int[][] board, Position position, int color) {
        if (board == null || position == null) {
            return false;
        }

        int x = position.getX();
        int y = position.getY();

        // Check 4 directions: horizontal, vertical, diagonal, anti-diagonal
        return checkDirection(board, x, y, color, 1, 0) ||   // Horizontal
                checkDirection(board, x, y, color, 0, 1) ||   // Vertical
                checkDirection(board, x, y, color, 1, 1) ||   // Diagonal (\)
                checkDirection(board, x, y, color, 1, -1);    // Anti-diagonal (/)
    }

    /**
     * Check if there are 5 or more stones in a row in a specific direction
     *
     * @param board the game board
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param color the player color value
     * @param dx    the x direction (-1, 0, or 1)
     * @param dy    the y direction (-1, 0, or 1)
     * @return true if 5 or more stones in a row, false otherwise
     */
    private static boolean checkDirection(int[][] board, int x, int y, int color, int dx, int dy) {
        int count = 1; // Count the current stone

        // Count in positive direction
        count += countStones(board, x, y, color, dx, dy);

        // Count in negative direction
        count += countStones(board, x, y, color, -dx, -dy);

        return count >= 5;
    }

    /**
     * Count consecutive stones in a specific direction
     *
     * @param board the game board
     * @param x     the starting x coordinate
     * @param y     the starting y coordinate
     * @param color the player color value
     * @param dx    the x direction
     * @param dy    the y direction
     * @return the count of consecutive stones
     */
    private static int countStones(int[][] board, int x, int y, int color, int dx, int dy) {
        int count = 0;
        int boardSize = board.length;
        int currentX = x + dx;
        int currentY = y + dy;

        while (isValidPosition(currentX, currentY, boardSize) &&
                board[currentX][currentY] == color) {
            count++;
            currentX += dx;
            currentY += dy;
        }

        return count;
    }

    /**
     * Check if a position is valid (within board bounds)
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param boardSize the board size
     * @return true if valid, false otherwise
     */
    private static boolean isValidPosition(int x, int y, int boardSize) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    /**
     * Check if the board is full (draw condition)
     *
     * @param board the game board
     * @return true if board is full, false otherwise
     */
    public static boolean isBoardFull(int[][] board) {
        if (board == null) {
            return false;
        }

        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) {
                    return false; // Found an empty cell
                }
            }
        }

        return true; // No empty cells found
    }
}
