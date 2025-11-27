package com.goody.nus.se.gomoku.gomoku.game.chain.validate.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for UndoValidateChain
 *
 * @author Claude
 * @version 1.0
 */
class UndoValidateChainTest {

    private UndoValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new UndoValidateChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .undoProposerColor(null)
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidate_Success() {
        // Add a move to history
        GameAction move = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .build();
        game.getActionHistory().add(move);

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenPendingUndoProposal() {
        // Add a move to history
        GameAction move = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .build();
        game.getActionHistory().add(move);
        game.setUndoProposerColor(PlayerColor.WHITE); // Pending undo proposal

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenNoMoves() {
        // No moves in history
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenOnlyNonMoveActions() {
        // Add non-MOVE actions
        GameAction ready = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();
        game.getActionHistory().add(ready);

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidate_SuccessWithMultipleMoves() {
        // Add multiple moves
        GameAction move1 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .build();
        GameAction move2 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(new Position(8, 8))
                .build();
        game.getActionHistory().add(move1);
        game.getActionHistory().add(move2);

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidate_SuccessWithMixedActions() {
        // Add mixed action types
        GameAction ready = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();
        GameAction move = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .build();
        game.getActionHistory().add(ready);
        game.getActionHistory().add(move);

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.UNDO));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
