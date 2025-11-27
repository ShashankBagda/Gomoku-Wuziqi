package com.goody.nus.se.gomoku.gomoku.game.chain.validate.draw;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that a player can propose a draw (only during PLAYING status)
 * Checks that there's no pending draw proposal
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class DrawValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Proposing a draw: check that there's no pending draw proposal
        if (game.hasPendingDrawProposal()) {
            log.warn("There's already a pending draw proposal from player {}", game.getDrawProposerColor());
            return false;
        }
        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.DRAW);
    }

    @Override
    public int sort() {
        return 1;
    }
}
