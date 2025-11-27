package com.goody.nus.se.gomoku.gomoku.game.chain.execute.draw;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes DRAW_DISAGREE action
 * Rejects the draw proposal and continues the game
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class DrawDisagreeExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // Player disagreed with draw, clear the proposal and continue game
        log.info("Player {} disagreed with draw proposal from {}, game continues",
                action.getColor(), game.getDrawProposerColor());

        // Clear draw proposal
        game.clearDrawProposal();
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
