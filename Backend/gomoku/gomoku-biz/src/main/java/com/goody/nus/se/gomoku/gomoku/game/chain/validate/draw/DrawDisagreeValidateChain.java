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
 * Validates that a player can disagree with a draw proposal
 * Checks that there's a pending draw proposal from the opponent
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class DrawDisagreeValidateChain implements ValidateChain {
    @Override
    public GameStatus validateStatus() {
        return GameStatus.PLAYING;
    }

    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Check that there IS a pending draw proposal
        if (!game.hasPendingDrawProposal()) {
            log.warn("No pending draw proposal to disagree with");
            return false;
        }

        // Check that the responder is NOT the same player who proposed
        if (game.getDrawProposerColor() == action.getColor()) {
            log.warn("Player cannot disagree with their own draw proposal");
            return false;
        }

        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.DRAW_DISAGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
