package com.goody.nus.se.gomoku.gomoku.game.chain.execute.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes UNDO_AGREE action
 * Reverts the last move from the board
 * Updates the game state and turn accordingly
 *
 * @author Haotian
 * @version 1.0, 2025/10/15
 */
@Slf4j
@Component
public class UndoAgreeExecuteChain extends ExecuteChain {
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    @Override
    public void execute(GameDocument game, GameAction action) {
        // Get the last MOVE actions from history
        List<GameAction> moveActions = game.getActionHistory()
                .stream()
                .filter(a -> a.getType() == ActionType.MOVE)
                .toList();

        if (moveActions.isEmpty()) {
            log.error("Cannot undo: no moves found in history");
            throw new IllegalStateException("Not enough moves to undo");
        }

        PlayerColor undoProposer = game.getUndoProposerColor();
        GameAction lastMove = moveActions.get(moveActions.size() - 1);

        // Determine how many moves to undo based on who made the last move:
        // - If the last move was made by the proposer: undo 1 move (proposer wants to undo their own mistake)
        // - If the last move was made by opponent: undo 2 moves (proposer wants to undo opponent's good move + their own previous move)
        int movesToUndo;
        if (lastMove.getColor() == undoProposer) {
            // Last move was by proposer - they want to undo their own mistake
            movesToUndo = 1;
        } else {
            // Last move was by opponent - proposer wants to undo both opponent's move and their own previous move
            movesToUndo = 2;
        }

        // Validate we have enough moves
        if (moveActions.size() < movesToUndo) {
            log.error("Cannot undo: need {} moves but only found {}", movesToUndo, moveActions.size());
            throw new IllegalStateException("Not enough moves to undo");
        }

        // Remove the last N moves from board and history
        StringBuilder logMessage = new StringBuilder();
        for (int i = 0; i < movesToUndo; i++) {
            GameAction moveToUndo = moveActions.get(moveActions.size() - 1 - i);
            Position pos = moveToUndo.getPosition();

            // Remove stone from board
            game.getCurrentState().getBoard()[pos.getX()][pos.getY()] = 0;

            // Remove from history
            game.getActionHistory().removeIf(a -> a == moveToUndo);

            if (i > 0) {
                logMessage.append(", ");
            }
            logMessage.append(String.format("(%d,%d)", pos.getX(), pos.getY()));
        }

        // Update total moves
        game.getCurrentState().setTotalMoves(game.getCurrentState().getTotalMoves() - movesToUndo);

        // Update current turn to the undo proposer (they get to replay)
        game.getCurrentState().setCurrentTurn(undoProposer);

        log.info("Player {} agreed to undo (proposed by {}). Reverted {} move(s) at positions: {}",
                action.getColor(),
                undoProposer,
                movesToUndo,
                logMessage);

        // Clear undo proposal
        game.clearUndoProposal();
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.UNDO_AGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
