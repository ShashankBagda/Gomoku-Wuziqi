package com.goody.nus.se.gomoku.gomoku.game.chain.execute.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChain;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes RESTART_AGREE action
 *
 * <p>This is the core restart logic that:
 * <ol>
 *   <li>Archives the current finished game to history</li>
 *   <li>Resets the game state for a new game</li>
 *   <li>Swaps player colors for fairness</li>
 *   <li>Updates room status back to MATCHED</li>
 * </ol>
 *
 * <p>Design principles:
 * <ul>
 *   <li>Data preservation: Complete game history is saved before reset</li>
 *   <li>Reusability: Uses GameDocument.resetForNewGame() for consistent reset logic</li>
 *   <li>Fairness: Players swap colors each game</li>
 * </ul>
 *
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RestartAgreeExecuteChain extends ExecuteChain {

    @Autowired
    private IGameHistoryService gameHistoryService;

    /**
     * Check if restart can be executed
     *
     * @param game The game document
     * @param action The restart agree action
     * @return Always true (validation is done in ValidateChain)
     */
    @Override
    public boolean check(GameDocument game, GameAction action) {
        return true;
    }

    /**
     * Execute RESTART_AGREE action
     *
     * <p>Archives the finished game and resets for a new game.
     *
     * @param game The game document
     * @param action The restart agree action
     */
    @Override
    public void execute(GameDocument game, GameAction action) {
        log.info("[Restart] Player {} agreed to restart (proposed by {}), archiving game and resetting",
                action.getColor(), game.getRestartProposerColor());

        // Step 1: Determine end reason from game state
        String endReason = determineEndReason(game);

        // Step 2: Get current game number before reset
        Integer currentGameNumber = game.getGameCount() != null ? game.getGameCount() : 1;

        // Step 3: Archive current game to history
        log.info("[Restart] Archiving game #{} to history: roomId={}", currentGameNumber, game.getRoomId());
        gameHistoryService.archiveGame(game, currentGameNumber, endReason);

        // Step 4: Reset game state (increments gameCount, swaps colors, clears state)
        log.info("[Restart] Resetting game state for new game #{}", currentGameNumber + 1);
        game.resetForNewGame();

        // Step 5: Update room status back to MATCHED (ready for new game)
        updateRoomStatus(game.getRoomId(), RoomStatusEnum.MATCHED);

        log.info("[Restart] Game restart completed: roomId={}, newGameNumber={}, blackPlayer={}, whitePlayer={}",
                game.getRoomId(), game.getGameCount(), game.getBlackPlayerId(), game.getWhitePlayerId());
    }

    /**
     * Determine end reason from game state
     *
     * @param game The game document
     * @return Human-readable end reason
     */
    private String determineEndReason(GameDocument game) {
        if (game.getCurrentState() == null) {
            return "UNKNOWN";
        }

        Integer winner = game.getCurrentState().getWinner();
        if (winner == null || winner == -1) {
            return "ONGOING"; // Shouldn't happen
        } else if (winner == 0) {
            return "DRAW";
        } else {
            // Check last action for more specific reason
            GameAction lastAction = game.getLastAction();
            if (lastAction != null) {
                ActionType lastType = lastAction.getType();
                if (lastType == ActionType.SURRENDER) {
                    return "SURRENDER";
                } else if (lastType == ActionType.TIMEOUT) {
                    return "TIMEOUT";
                } else if (lastType == ActionType.DRAW_AGREE) {
                    return "DRAW";
                }
            }
            return "WIN"; // Normal win by making 5 in a row
        }
    }

    @Override
    public List<ActionType> getActionTypes() {
        return List.of(ActionType.RESTART_AGREE);
    }

    @Override
    public int sort() {
        return 1;
    }
}
