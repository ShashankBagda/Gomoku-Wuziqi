package com.goody.nus.se.gomoku.gomoku.game.chain.execute.ready;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for SingleReadyExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class SingleReadyExecuteChainTest {

    private SingleReadyExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new SingleReadyExecuteChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .blackReady(false)
                .whiteReady(false)
                .build();
    }

    @Test
    void testCheckWhenBlackReadyAndWhiteNotReady() {
        game.setWhiteReady(false);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWhenWhiteReadyAndBlackNotReady() {
        game.setBlackReady(false);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWhenOpponentAlreadyReady() {
        game.setWhiteReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.check(game, action));
    }

    @Test
    void testExecuteBlackReady() {
        game.setWhiteReady(false);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify black is marked as ready
        assertTrue(game.getBlackReady());

        // Verify white is still not ready
        assertFalse(game.getWhiteReady());

        // Verify game status remains WAITING
        assertEquals(GameStatus.WAITING, game.getStatus());
    }

    @Test
    void testExecuteWhiteReady() {
        game.setBlackReady(false);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify white is marked as ready
        assertTrue(game.getWhiteReady());

        // Verify black is still not ready
        assertFalse(game.getBlackReady());

        // Verify game status remains WAITING
        assertEquals(GameStatus.WAITING, game.getStatus());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.READY));
    }

    @Test
    void testSort() {
        assertEquals(2, chain.sort());
    }
}
