package com.goody.nus.se.gomoku.gomoku.game.chain.validate.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that it's the player's turn to move
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class TurnValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        PlayerColor currentTurn = game.getCurrentState().getCurrentTurn();
        PlayerColor actionColor = action.getColor();

        if (currentTurn == null) {
            log.error("Current turn is null in PLAYING status");
            return false;
        }

        if (actionColor == null) {
            log.error("Action color is null: {}", action);
            return false;
        }

        if (currentTurn != actionColor) {
            log.warn("Not player's turn. Current turn: {}, Action color: {}", currentTurn, actionColor);
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
        return 2; // Check turn after board size
    }
}
