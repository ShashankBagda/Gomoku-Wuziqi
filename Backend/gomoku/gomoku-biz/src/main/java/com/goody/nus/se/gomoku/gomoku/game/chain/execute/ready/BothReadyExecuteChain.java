package com.goody.nus.se.gomoku.gomoku.game.chain.execute.ready;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes ready action when both players are ready
 * Starts the game by transitioning to PLAYING status
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class BothReadyExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        // This chain handles when both players will be ready after this action
        PlayerColor color = action.getColor();

        if (color == PlayerColor.BLACK) {
            // After black marks ready, check if white is already ready
            return game.getWhiteReady();
        } else {
            // After white marks ready, check if black is already ready
            return game.getBlackReady();
        }
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        PlayerColor color = action.getColor();

        // Mark player as ready
        if (color == PlayerColor.BLACK) {
            game.setBlackPlayerId(action.getPlayerId());
            game.setBlackReady(true);
        } else {
            game.setWhitePlayerId(action.getPlayerId());
            game.setWhiteReady(true);
        }

        // Both players are ready, start the game
        game.setStatus(GameStatus.PLAYING);

        // Set initial turn to BLACK (BLACK always goes first in Gomoku)
        game.getCurrentState().setCurrentTurn(PlayerColor.BLACK);

        // Update room status to PLAYING
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.PLAYING);

        log.info("Both players ready, game started. BLACK player goes first.");
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.READY);
    }

    @Override
    public int sort() {
        return 1; // Check both ready first
    }
}
