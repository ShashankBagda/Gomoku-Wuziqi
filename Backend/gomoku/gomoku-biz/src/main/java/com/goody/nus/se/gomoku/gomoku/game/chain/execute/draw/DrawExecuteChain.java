package com.goody.nus.se.gomoku.gomoku.game.chain.execute.draw;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes DRAW proposal action
 * Records the draw proposal and waits for opponent's response
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class DrawExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // Record who proposed the draw
        game.setDrawProposerColor(action.getColor());

        log.info("Player {} proposed a draw, waiting for opponent's response (DRAW_AGREE or DRAW_DISAGREE)",
                action.getColor());
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
