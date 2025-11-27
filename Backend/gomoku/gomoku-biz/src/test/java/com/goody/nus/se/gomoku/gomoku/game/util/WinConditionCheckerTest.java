package com.goody.nus.se.gomoku.gomoku.game.util;

import com.goody.nus.se.gomoku.gomoku.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for WinConditionChecker
 *
 * @author Claude
 * @version 1.0
 */
class WinConditionCheckerTest {

    @Test
    void testCheckWin_NullBoard() {
        assertFalse(WinConditionChecker.checkWin(null, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_NullPosition() {
        int[][] board = new int[15][15];
        assertFalse(WinConditionChecker.checkWin(board, null, 1));
    }

    @Test
    void testCheckWin_HorizontalWin() {
        int[][] board = new int[15][15];
        // Place 5 black stones horizontally
        for (int i = 5; i < 10; i++) {
            board[7][i] = 1; // BLACK
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_VerticalWin() {
        int[][] board = new int[15][15];
        // Place 5 white stones vertically
        for (int i = 3; i < 8; i++) {
            board[i][7] = 2; // WHITE
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(5, 7), 2));
    }

    @Test
    void testCheckWin_DiagonalWin() {
        int[][] board = new int[15][15];
        // Place 5 black stones diagonally (\)
        for (int i = 0; i < 5; i++) {
            board[5 + i][5 + i] = 1; // BLACK
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_AntiDiagonalWin() {
        int[][] board = new int[15][15];
        // Place 5 white stones anti-diagonally (/)
        for (int i = 0; i < 5; i++) {
            board[10 - i][5 + i] = 2; // WHITE
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(8, 7), 2));
    }

    @Test
    void testCheckWin_NoWin_OnlyFour() {
        int[][] board = new int[15][15];
        // Place only 4 black stones horizontally
        for (int i = 5; i < 9; i++) {
            board[7][i] = 1; // BLACK
        }

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_NoWin_Mixed() {
        int[][] board = new int[15][15];
        // Place 4 black stones with 1 white in between
        board[7][5] = 1;
        board[7][6] = 1;
        board[7][7] = 2; // WHITE breaks the sequence
        board[7][8] = 1;
        board[7][9] = 1;

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 5), 1));
    }

    @Test
    void testCheckWin_MoreThanFive() {
        int[][] board = new int[15][15];
        // Place 6 black stones horizontally
        for (int i = 5; i < 11; i++) {
            board[7][i] = 1; // BLACK
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(7, 8), 1));
    }

    @Test
    void testCheckWin_EdgeOfBoard_Horizontal() {
        int[][] board = new int[15][15];
        // Place 5 stones at the edge
        for (int i = 0; i < 5; i++) {
            board[0][i] = 1; // BLACK at top edge
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(0, 2), 1));
    }

    @Test
    void testCheckWin_EdgeOfBoard_Vertical() {
        int[][] board = new int[15][15];
        // Place 5 stones at the edge
        for (int i = 0; i < 5; i++) {
            board[i][0] = 2; // WHITE at left edge
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(2, 0), 2));
    }

    @Test
    void testCheckWin_CornerDiagonal() {
        int[][] board = new int[15][15];
        // Place 5 stones diagonally from corner
        for (int i = 0; i < 5; i++) {
            board[i][i] = 1; // BLACK
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(2, 2), 1));
    }

    @Test
    void testCheckWin_BottomRightCorner() {
        int[][] board = new int[15][15];
        // Place 5 stones near bottom right
        for (int i = 0; i < 5; i++) {
            board[14][10 + i] = 2; // WHITE
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(14, 12), 2));
    }

    @Test
    void testIsBoardFull_NullBoard() {
        assertFalse(WinConditionChecker.isBoardFull(null));
    }

    @Test
    void testIsBoardFull_EmptyBoard() {
        int[][] board = new int[15][15];
        assertFalse(WinConditionChecker.isBoardFull(board));
    }

    @Test
    void testIsBoardFull_PartiallyFilled() {
        int[][] board = new int[15][15];
        // Fill half the board
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = 1;
            }
        }

        assertFalse(WinConditionChecker.isBoardFull(board));
    }

    @Test
    void testIsBoardFull_FullBoard() {
        int[][] board = new int[15][15];
        // Fill the entire board
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = (i + j) % 2 + 1; // Alternate BLACK and WHITE
            }
        }

        assertTrue(WinConditionChecker.isBoardFull(board));
    }

    @Test
    void testIsBoardFull_AlmostFull() {
        int[][] board = new int[15][15];
        // Fill the board except one cell
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = 1;
            }
        }
        board[7][7] = 0; // One empty cell

        assertFalse(WinConditionChecker.isBoardFull(board));
    }

    @Test
    void testCheckWin_SingleStone() {
        int[][] board = new int[15][15];
        board[7][7] = 1; // Only one stone

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_TwoStones() {
        int[][] board = new int[15][15];
        board[7][7] = 1;
        board[7][8] = 1;

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
    }

    @Test
    void testCheckWin_ThreeStones() {
        int[][] board = new int[15][15];
        board[7][7] = 1;
        board[7][8] = 1;
        board[7][9] = 1;

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 8), 1));
    }

    @Test
    void testCheckWin_FourStones() {
        int[][] board = new int[15][15];
        board[7][7] = 1;
        board[7][8] = 1;
        board[7][9] = 1;
        board[7][10] = 1;

        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 8), 1));
    }

    @Test
    void testCheckWin_FiveStonesVertical_CheckFromDifferentPositions() {
        int[][] board = new int[15][15];
        for (int i = 5; i < 10; i++) {
            board[i][7] = 1;
        }

        // Check from each position in the winning line
        assertTrue(WinConditionChecker.checkWin(board, new Position(5, 7), 1));
        assertTrue(WinConditionChecker.checkWin(board, new Position(6, 7), 1));
        assertTrue(WinConditionChecker.checkWin(board, new Position(7, 7), 1));
        assertTrue(WinConditionChecker.checkWin(board, new Position(8, 7), 1));
        assertTrue(WinConditionChecker.checkWin(board, new Position(9, 7), 1));
    }

    @Test
    void testCheckWin_AntiDiagonal_AtBoardEdge() {
        int[][] board = new int[15][15];
        // Anti-diagonal at top right
        for (int i = 0; i < 5; i++) {
            board[i][14 - i] = 2;
        }

        assertTrue(WinConditionChecker.checkWin(board, new Position(2, 12), 2));
    }

    @Test
    void testCheckWin_WrongColor() {
        int[][] board = new int[15][15];
        // Place 5 black stones
        for (int i = 5; i < 10; i++) {
            board[7][i] = 1;
        }

        // Check with wrong color (WHITE)
        assertFalse(WinConditionChecker.checkWin(board, new Position(7, 7), 2));
    }
}
