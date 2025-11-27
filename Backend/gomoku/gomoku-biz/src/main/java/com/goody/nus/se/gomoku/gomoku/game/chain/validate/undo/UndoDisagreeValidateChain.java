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
 * Validates that a player can disagree with an undo proposal
 * Checks that there's a pending undo proposal
 * Checks that the player disagreeing is NOT the one who proposed
 *
 * @author Haotian
 * @version 1.0, 2025/10/15
 */
@Slf4j
@Component
public class UndoDisagreeValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Must have a pending undo proposal
        if (!game.hasPendingUndoProposal()) {
            log.warn("No pending undo proposal to disagree with");
            return false;
        }

        // Player disagreeing must NOT be the one who proposed
        if (game.getUndoProposerColor() == action.getColor()) {
            log.warn("Player {} cannot disagree with their own undo proposal", action.getColor());
            return false;
        }

        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.UNDO_DISAGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
