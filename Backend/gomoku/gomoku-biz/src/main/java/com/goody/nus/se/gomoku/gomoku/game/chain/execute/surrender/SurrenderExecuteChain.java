package com.goody.nus.se.gomoku.gomoku.game.chain.execute.surrender;

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
 * Executes surrender action
 * Ends the game immediately with opponent as winner
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class SurrenderExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        // Surrender always applies
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // The player who surrenders loses, opponent wins
        // Set winner to opponent's color value
        int opponentValue = action.getColor().getOpponent().getValue();
        game.getCurrentState().setWinner(opponentValue);

        // End the game
        game.setStatus(GameStatus.FINISHED);

        // Update room status to FINISHED
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.FINISHED);

        log.info("Player {} surrendered, player {} wins",
                action.getColor(), action.getColor().getOpponent());
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.SURRENDER);
    }

    @Override
    public int sort() {
        return 1;
    }
}
