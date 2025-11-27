package com.goody.nus.se.gomoku.gomoku.game.chain.validate.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that a player can propose an undo (only during PLAYING status)
 * Checks that there's no pending undo proposal
 * Checks that there's at least 1 move in history to undo
 *
 * @author Haotian
 * @version 1.0, 2025/10/15
 */
@Slf4j
@Component
public class UndoValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Check that there's no pending undo proposal
        if (game.hasPendingUndoProposal()) {
            log.warn("There's already a pending undo proposal from player {}", game.getUndoProposerColor());
            return false;
        }

        // Check that there are moves to undo (at least 1 MOVE action in history)
        long moveCount = game.getActionHistory().stream()
                .filter(a -> a.getType() == ActionType.MOVE)
                .count();

        if (moveCount < 1) {
            log.warn("Cannot propose undo: need at least 1 move in history, but have {}", moveCount);
            return false;
        }

        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.UNDO);
    }

    @Override
    public int sort() {
        return 1;
    }
}
