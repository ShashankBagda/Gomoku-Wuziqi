package com.goody.nus.se.gomoku.gomoku.game.chain.execute.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes a move that results in board full (draw)
 * Places stone, sets draw, ends game
 * This chain handles the case where the board becomes full after the move
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class FullExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        Position position = action.getPosition();
        GameStateSnapshot state = game.getCurrentState();
        int[][] board = state.getBoard();
        int boardSize = board.length;

        // Check if all positions except the current one are occupied
        // If yes, placing this stone will make the board full
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                // Skip the position where we're about to place the stone
                if (i == position.getX() && j == position.getY()) {
                    continue;
                }
                // If we find any empty cell, board won't be full after this move
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        // All other positions are occupied, this move will fill the board
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        GameStateSnapshot state = game.getCurrentState();
        Position position = action.getPosition();

        // Clear any pending draw proposal (making a move implicitly rejects draw)
        if (game.hasPendingDrawProposal()) {
            log.info("Player {} made a move that fills the board, implicitly rejecting draw proposal from {}",
                    action.getColor(), game.getDrawProposerColor());
            game.clearDrawProposal();
        }

        // Place the stone
        state.getBoard()[position.getX()][position.getY()] = action.getColor().getValue();

        // Increment move count
        state.setTotalMoves(state.getTotalMoves() + 1);

        // Board is full, it's a draw
        state.setWinner(0); // 0 indicates draw
        game.setStatus(com.goody.nus.se.gomoku.gomoku.enums.GameStatus.FINISHED);

        // Update snapshot time
        state.setSnapshotTime(System.currentTimeMillis());

        // Update room status to FINISHED
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.FINISHED);

        log.info("Board is full at position ({}, {}), game ended in a draw",
                position.getX(), position.getY());
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.MOVE);
    }

    @Override
    public int sort() {
        return 2; // Check after win, before normal move
    }
}
