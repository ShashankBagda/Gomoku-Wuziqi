package com.goody.nus.se.gomoku.gomoku.game.chain.execute.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes UNDO proposal action
 * Records the undo proposal and waits for opponent's response
 *
 * @author Haotian
 * @version 1.0, 2025/10/15
 */
@Slf4j
@Component
public class UndoExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // Record who proposed the undo
        game.setUndoProposerColor(action.getColor());

        log.info("Player {} proposed an undo, waiting for opponent's response (UNDO_AGREE or UNDO_DISAGREE)",
                action.getColor());
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
