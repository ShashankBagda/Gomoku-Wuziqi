package com.goody.nus.se.gomoku.gomoku.game.chain.validate.timeout;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates timeout action (system triggered when player runs out of time)
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class TimeoutFailValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Timeout is a system action, player cannot trigger it manually
        return false;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.TIMEOUT);
    }

    @Override
    public int sort() {
        return 1;
    }
}
