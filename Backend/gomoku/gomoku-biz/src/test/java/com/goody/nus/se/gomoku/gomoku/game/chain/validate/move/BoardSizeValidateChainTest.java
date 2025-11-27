package com.goody.nus.se.gomoku.gomoku.game.chain.validate.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for BoardSizeValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class BoardSizeValidateChainTest {

    private BoardSizeValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new BoardSizeValidateChain();
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
    void testValidateWithValidPosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateWithPositionOutOfBounds() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(15).y(7).build())
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNegativePosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(-1).y(7).build())
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullPosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(null)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.MOVE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
