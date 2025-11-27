package com.goody.nus.se.gomoku.gomoku.game.chain.execute.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes RESTART proposal action
 *
 * <p>Records the restart proposal and waits for opponent's response.
 * The actual game reset happens in RestartAgreeExecuteChain.
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RestartExecuteChain extends ExecuteChain {

    /**
     * Check if restart can be executed
     *
     * @param game The game document
     * @param action The restart action
     * @return Always true (validation is done in ValidateChain)
     */
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    /**
     * Execute RESTART proposal
     *
     * <p>Records the proposer and waits for opponent's response.
     *
     * @param game The game document
     * @param action The restart action
     */
    @Override
    public void execute(GameDocument game, GameAction action) {
        // Record who proposed the restart
        game.setRestartProposerColor(action.getColor());

        log.info("[Restart] Player {} proposed to restart the game, waiting for opponent's response (RESTART_AGREE or RESTART_DISAGREE)",
                action.getColor());
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
