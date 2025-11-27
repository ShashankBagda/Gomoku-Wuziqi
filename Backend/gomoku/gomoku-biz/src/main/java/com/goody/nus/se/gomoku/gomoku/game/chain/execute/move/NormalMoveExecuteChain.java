package com.goody.nus.se.gomoku.gomoku.game.chain.execute.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes a normal move that doesn't result in a win
 * Places stone, switches turn, updates state
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
@Component
public class NormalMoveExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        // In WinOrLoseExecuteChain we already checked if this move results in a win
        // If it did, this chain should not execute
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        GameStateSnapshot state = game.getCurrentState();
        Position position = action.getPosition();

        // Clear any pending draw proposal (making a move implicitly rejects draw)
        if (game.hasPendingDrawProposal()) {
            log.info("Player {} made a move, implicitly rejecting draw proposal from {}",
                    action.getColor(), game.getDrawProposerColor());
            game.clearDrawProposal();
        }

        // Clear any pending undo proposal (making a move implicitly rejects undo)
        if (game.hasPendingUndoProposal()) {
            log.info("Player {} made a move, implicitly rejecting undo proposal from {}",
                    action.getColor(), game.getUndoProposerColor());
            game.clearUndoProposal();
        }

        // Place the stone
        state.getBoard()[position.getX()][position.getY()] = action.getColor().getValue();

        // Increment move count
        state.setTotalMoves(state.getTotalMoves() + 1);

        // Switch turn to opponent
        state.setCurrentTurn(action.getColor().getOpponent());

        // Update snapshot time
        state.setSnapshotTime(System.currentTimeMillis());

        log.info("Normal move executed at ({}, {}) by {}",
                position.getX(), position.getY(), action.getColor());
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.MOVE);
    }

    @Override
    public int sort() {
        return 3; // Check after WinOrLose chain
    }
}
