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
 * Test for StonePositionValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class StonePositionValidateChainTest {

    private StonePositionValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new StonePositionValidateChain();

        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        // Place a black stone at (5, 5)
        state.getBoard()[5][5] = PlayerColor.BLACK.getValue();

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
    void testValidateWithEmptyPosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateWithOccupiedPosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(Position.builder().x(5).y(5).build())
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullPosition() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(null)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidateWithNullBoard() {
        game.getCurrentState().setBoard(null);

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
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
        assertEquals(3, chain.sort());
    }
}
