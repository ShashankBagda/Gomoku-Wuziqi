package com.goody.nus.se.gomoku.gomoku.game.chain.validate.ready;

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
 * Validates that a player can mark themselves as ready
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class ReadyValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.WAITING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        PlayerColor color = action.getColor();
        if (color == null) {
            log.error("Action color is null: {}", action);
            return false;
        }

        // Check if player is already ready
        boolean alreadyReady = (color == PlayerColor.BLACK && Boolean.TRUE.equals(game.getBlackReady())) ||
                (color == PlayerColor.WHITE && Boolean.TRUE.equals(game.getWhiteReady()));

        if (alreadyReady) {
            log.warn("Player {} is already ready", color);
            return false;
        }

        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.READY);
    }

    @Override
    public int sort() {
        return 1;
    }
}
