package com.goody.nus.se.gomoku.gomoku.game.chain.validate.surrender;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that a player can surrender (only during PLAYING status)
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class SurrenderPlayingValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Surrender is always valid during PLAYING status
        // The player just needs to be in the game, which is already validated by the service
        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.SURRENDER);
    }

    @Override
    public int sort() {
        return 1;
    }
}
