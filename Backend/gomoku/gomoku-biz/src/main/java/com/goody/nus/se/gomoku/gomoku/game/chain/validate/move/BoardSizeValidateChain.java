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
 * Validates that the move position is within the board bounds
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class BoardSizeValidateChain implements ValidateChain {
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

        Integer boardSize = game.getCurrentState().getBoardSize();
        if (boardSize == null) {
            boardSize = 15; // Default board size
        }

        boolean isValid = position.isValid(boardSize);
        if (!isValid) {
            log.warn("Invalid position {} for board size {}", position, boardSize);
        }
        return isValid;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.MOVE);
    }

    @Override
    public int sort() {
        return 1; // Check board bounds first
    }
}
