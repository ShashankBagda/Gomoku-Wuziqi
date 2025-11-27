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
 * Test for TurnValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class TurnValidateChainTest {

    private TurnValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new TurnValidateChain();

        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setCurrentTurn(PlayerColor.BLACK);

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(state)
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidateWithCorrectTurn() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateWithWrongTurn() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullCurrentTurn() {
        game.getCurrentState().setCurrentTurn(null);

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullActionColor() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(null)
                .position(Position.builder().x(7).y(7).build())
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
        assertEquals(2, chain.sort());
    }
}
