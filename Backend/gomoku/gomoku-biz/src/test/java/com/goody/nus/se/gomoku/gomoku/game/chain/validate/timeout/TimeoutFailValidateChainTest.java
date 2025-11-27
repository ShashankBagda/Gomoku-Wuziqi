package com.goody.nus.se.gomoku.gomoku.game.chain.validate.timeout;

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
 * Test for TimeoutFailValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class TimeoutFailValidateChainTest {

    private TimeoutFailValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new TimeoutFailValidateChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidateAlwaysReturnsFalse() {
        GameAction action = GameAction.builder()
                .type(ActionType.TIMEOUT)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        // Timeout is a system action, players cannot trigger it manually
        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithDifferentPlayers() {
        GameAction blackAction = GameAction.builder()
                .type(ActionType.TIMEOUT)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        GameAction whiteAction = GameAction.builder()
                .type(ActionType.TIMEOUT)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, blackAction));
        assertFalse(chain.validate(game, whiteAction));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.TIMEOUT));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
