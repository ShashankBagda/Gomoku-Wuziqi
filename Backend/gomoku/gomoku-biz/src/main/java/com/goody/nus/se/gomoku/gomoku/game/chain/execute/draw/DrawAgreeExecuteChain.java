package com.goody.nus.se.gomoku.gomoku.game.chain.execute.draw;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes DRAW_AGREE action
 * Ends the game with a draw result
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class DrawAgreeExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // Both players agreed, end game with draw
        game.getCurrentState().setWinner(0); // 0 indicates draw
        game.setStatus(GameStatus.FINISHED);

        // Update room status to FINISHED
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.FINISHED);

        log.info("Player {} agreed to draw (proposed by {}), game ended in a draw",
                action.getColor(), game.getDrawProposerColor());

        // Clear draw proposal
        game.clearDrawProposal();
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.DRAW_AGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
