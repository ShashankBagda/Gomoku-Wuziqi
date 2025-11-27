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
 * Unit test for RestartExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class RestartExecuteChainTest {

    private RestartExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new RestartExecuteChain();

        // Create a finished game
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1); // Black wins

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(state)
                .restartProposerColor(null)
                .build();
    }

    @Test
    @DisplayName("Should always pass check")
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    @DisplayName("Should set restart proposer when RESTART is executed")
    void testExecuteRestartProposal() {
        assertNull(game.getRestartProposerColor());

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify restart proposer is set
        assertEquals(PlayerColor.BLACK, game.getRestartProposerColor());

        // Verify game is still finished
        assertEquals(GameStatus.FINISHED, game.getStatus());
    }

    @Test
    @DisplayName("Should handle WHITE player proposing restart")
    void testExecuteRestartProposalByWhite() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        assertEquals(PlayerColor.WHITE, game.getRestartProposerColor());
    }

    @Test
    @DisplayName("Should return correct action types")
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.RESTART));
    }

    @Test
    @DisplayName("Should return correct sort order")
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
