package com.goody.nus.se.gomoku.gomoku.game.chain.execute.ready;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes ready action when only one player is ready (not both)
 * Just marks the player as ready, game stays in WAITING status
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class SingleReadyExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        // This chain handles when only one player will be ready after this action
        PlayerColor color = action.getColor();

        if (color == PlayerColor.BLACK) {
            // After black marks ready, white is still not ready
            return !game.getWhiteReady();
        } else {
            // After white marks ready, black is still not ready
            return !game.getBlackReady();
        }
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        PlayerColor color = action.getColor();

        // Mark player as ready
        if (color == PlayerColor.BLACK) {
            game.setBlackPlayerId(action.getPlayerId());
            game.setBlackReady(true);
            log.info("Black player marked as ready");
        } else {
            game.setWhitePlayerId(action.getPlayerId());
            game.setWhiteReady(true);
            log.info("White player marked as ready");
        }

        // Status remains WAITING
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.READY);
    }

    @Override
    public int sort() {
        return 2; // Check after BothReady
    }
}
