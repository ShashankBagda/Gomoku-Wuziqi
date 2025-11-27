package com.goody.nus.se.gomoku.gomoku.model;

import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Game state snapshot
 * Represents the current state of the game board and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Board size (default 15 for standard Gomoku)
     */
    @Builder.Default
    private Integer boardSize = 15;

    /**
     * Board: 0=empty, 1=black, 2=white
     */
    private int[][] board;

    /**
     * Current turn (BLACK or WHITE)
     * Null during WAITING phase
     */
    private PlayerColor currentTurn;

    /**
     * Winner: null=ongoing, 1=black, 2=white, 0=draw
     */
    private Integer winner;

    /**
     * Total number of moves made
     */
    private Integer totalMoves;

    /**
     * Timestamp of this snapshot
     */
    private Long snapshotTime;

    /**
     * Initialize an empty board
     */
    public static GameStateSnapshot createEmpty() {
        return createEmpty(15);
    }

    /**
     * Initialize an empty board with custom size
     */
    public static GameStateSnapshot createEmpty(int boardSize) {
        return GameStateSnapshot.builder()
                .boardSize(boardSize)
                .board(new int[boardSize][boardSize])
                .currentTurn(PlayerColor.BLACK)
                .winner(-1)
                .totalMoves(0)
                .snapshotTime(System.currentTimeMillis())
                .build();
    }

    /**
     * Create a deep copy of the current state
     */
    public GameStateSnapshot deepCopy() {
        int size = this.boardSize != null ? this.boardSize : 15;
        int[][] newBoard = new int[size][size];
        if (this.board != null) {
            for (int i = 0; i < size; i++) {
                System.arraycopy(this.board[i], 0, newBoard[i], 0, size);
            }
        }

        return GameStateSnapshot.builder()
                .boardSize(this.boardSize)
                .board(newBoard)
                .currentTurn(this.currentTurn)
                .winner(this.winner)
                .totalMoves(this.totalMoves)
                .snapshotTime(System.currentTimeMillis())
                .build();
    }
}
