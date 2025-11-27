package com.goody.nus.se.gomoku.gomoku.game.chain.validate.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that a player can agree to a restart proposal
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Game must be in FINISHED status</li>
 *   <li>A pending restart proposal must exist</li>
 *   <li>The responding player must be the opponent (not the proposer)</li>
 * </ul>
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RestartAgreeValidateChain implements ValidateChain {

    @Override
    public GameStatus validateStatus() {
        return GameStatus.FINISHED;
    }

    /**
     * Validate RESTART_AGREE response
     *
     * @param game The game document
     * @param action The restart agree action
     * @return true if validation passes, false otherwise
     */
    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Check that there's a pending restart proposal
        if (!game.hasPendingRestartProposal()) {
            log.warn("[Restart] Validation failed: No pending restart proposal to agree to");
            return false;
        }

        // Check that the responder is the opponent (not the proposer)
        if (game.getRestartProposerColor() == action.getColor()) {
            log.warn("[Restart] Validation failed: Proposer {} cannot agree to their own proposal",
                    action.getPlayerId());
            return false;
        }

        log.debug("[Restart] Validation passed: Player {} can agree to restart", action.getPlayerId());
        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.RESTART_AGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
