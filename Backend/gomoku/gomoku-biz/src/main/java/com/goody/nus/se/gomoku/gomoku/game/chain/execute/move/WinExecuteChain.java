package com.goody.nus.se.gomoku.gomoku.game.chain.execute.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.game.util.WinConditionChecker;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes a move that results in a win or a draw
 * Places stone, sets winner, ends game
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class WinExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        // Check if this move results in a win or draw
        Position position = action.getPosition();
        GameStateSnapshot state = game.getCurrentState();

        return WinConditionChecker.checkWin(state.getBoard(), position, action.getColor().getValue());
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        GameStateSnapshot state = game.getCurrentState();
        Position position = action.getPosition();

        // Clear any pending draw proposal (making a move implicitly rejects draw)
        if (game.hasPendingDrawProposal()) {
            log.info("Player {} made a winning move, implicitly rejecting draw proposal from {}",
                    action.getColor(), game.getDrawProposerColor());
            game.clearDrawProposal();
        }

        // Place the stone
        state.getBoard()[position.getX()][position.getY()] = action.getColor().getValue();

        // Increment move count
        state.setTotalMoves(state.getTotalMoves() + 1);

        // Check isWin is already true from check()
        // Set winner (1 for BLACK, 2 for WHITE)
        state.setWinner(action.getColor().getValue());
        log.info("Player {} wins at position ({}, {})", action.getColor(), position.getX(), position.getY());

        // Update snapshot time
        state.setSnapshotTime(System.currentTimeMillis());

        // Update game document
        game.setStatus(GameStatus.FINISHED);

        // Update room status to FINISHED
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.FINISHED);
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.MOVE);
    }

    @Override
    public int sort() {
        return 1; // Check win/lose first
    }
}
