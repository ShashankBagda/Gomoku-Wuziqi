package com.goody.nus.se.gomoku.gomoku.game.chain.validate.ready;

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
 * Test for ReadyValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class ReadyValidateChainTest {

    private ReadyValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new ReadyValidateChain();
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
    void testValidateStatus() {
        assertEquals(GameStatus.WAITING, chain.validateStatus());
    }

    @Test
    void testValidateBlackPlayerNotReady() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateWhitePlayerNotReady() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateBlackPlayerAlreadyReady() {
        game.setBlackReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWhitePlayerAlreadyReady() {
        game.setWhiteReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullColor() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(null)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.READY));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
