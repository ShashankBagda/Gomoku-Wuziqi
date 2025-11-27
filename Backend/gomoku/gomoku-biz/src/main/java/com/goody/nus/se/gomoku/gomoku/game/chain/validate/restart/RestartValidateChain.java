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
 * Validates that a player can propose a restart
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Game must be in FINISHED status</li>
 *   <li>No pending restart proposal exists</li>
 * </ul>
 *
 * <p>This prevents spam proposals and ensures only finished games can be restarted.
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RestartValidateChain implements ValidateChain {

    /**
     * RESTART can only be proposed after game is finished
     */
    @Override
    public GameStatus validateStatus() {
        return GameStatus.FINISHED;
    }

    /**
     * Validate RESTART proposal
     *
     * @param game The game document
     * @param action The restart action
     * @return true if validation passes, false otherwise
     */
    @Override
    public boolean validate(GameDocument game, GameAction action) {
        // Check that there's no pending restart proposal
        if (game.hasPendingRestartProposal()) {
            log.warn("[Restart] Validation failed: Pending restart proposal already exists from {}",
                    game.getRestartProposerColor());
            return false;
        }

        log.debug("[Restart] Validation passed: Player {} can propose restart", action.getPlayerId());
        return true;
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.RESTART);
    }

    @Override
    public int sort() {
        return 1;
    }
}
