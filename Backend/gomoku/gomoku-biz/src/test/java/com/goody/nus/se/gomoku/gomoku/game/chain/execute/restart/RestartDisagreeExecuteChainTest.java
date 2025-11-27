package com.goody.nus.se.gomoku.gomoku.game.chain.execute.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for RestartDisagreeExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class RestartDisagreeExecuteChainTest {

    private RestartDisagreeExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new RestartDisagreeExecuteChain();

        // Create a finished game with pending restart proposal
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(2); // White wins

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(state)
                .restartProposerColor(PlayerColor.WHITE) // White proposed restart
                .gameCount(1)
                .build();
    }

    @Test
    @DisplayName("Should always pass check")
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    @DisplayName("Should clear restart proposal when RESTART_DISAGREE is executed")
    void testExecuteRestartDisagree() {
        assertEquals(PlayerColor.WHITE, game.getRestartProposerColor());

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify restart proposal is cleared
        assertNull(game.getRestartProposerColor());

        // Verify game remains finished
        assertEquals(GameStatus.FINISHED, game.getStatus());

        // Verify game state unchanged
        assertEquals(2, game.getCurrentState().getWinner());
        assertEquals(1, game.getGameCount());
    }

    @Test
    @DisplayName("Should handle BLACK player disagreeing")
    void testExecuteRestartDisagreeByBlack() {
        game.setRestartProposerColor(PlayerColor.WHITE);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        chain.execute(game, action);

        assertNull(game.getRestartProposerColor());
        assertEquals(GameStatus.FINISHED, game.getStatus());
    }

    @Test
    @DisplayName("Should return correct action types")
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.RESTART_DISAGREE));
    }

    @Test
    @DisplayName("Should return correct sort order")
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
