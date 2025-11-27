package com.goody.nus.se.gomoku.gomoku.game.chain.validate.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that the position on the board is empty (not already occupied)
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class StonePositionValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        Position position = action.getPosition();
        if (position == null) {
            log.warn("Move action has no position: {}", action);
            return false;
        }

        int[][] board = game.getCurrentState().getBoard();
        if (board == null) {
            log.error("Game board is null");
            return false;
        }

        int x = position.getX();
        int y = position.getY();

        // Check if position is already occupied
        if (board[x][y] != 0) {
            log.warn("Position ({}, {}) is already occupied by player {}", x, y, board[x][y]);
            return false;
        }

        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.MOVE);
    }

    @Override
    public int sort() {
        return 3; // Check after board size and turn validation
    }
}
