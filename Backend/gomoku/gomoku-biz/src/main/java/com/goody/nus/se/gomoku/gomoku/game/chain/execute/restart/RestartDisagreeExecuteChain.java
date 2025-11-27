package com.goody.nus.se.gomoku.gomoku.game.chain.execute.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes RESTART_DISAGREE action
 *
 * <p>Clears the restart proposal and keeps the game in FINISHED state.
 * Players can still propose restart again later if desired.
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RestartDisagreeExecuteChain extends ExecuteChain {

    /**
     * Check if restart disagree can be executed
     *
     * @param game The game document
     * @param action The restart disagree action
     * @return Always true (validation is done in ValidateChain)
     */
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    /**
     * Execute RESTART_DISAGREE action
     *
     * <p>Simply clears the proposal. Game remains in FINISHED state.
     *
     * @param game The game document
     * @param action The restart disagree action
     */
    @Override
    public void execute(GameDocument game, GameAction action) {
        log.info("[Restart] Player {} disagreed with restart proposal (proposed by {}), clearing proposal",
                action.getColor(), game.getRestartProposerColor());

        // Clear restart proposal
        game.clearRestartProposal();

        // Game remains in FINISHED state
        log.debug("[Restart] Restart proposal cleared, game remains finished");
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.RESTART_DISAGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
