package com.goody.nus.se.gomoku.gomoku.game.chain.execute.undo;

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
 * Test for UndoAgreeExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class UndoAgreeExecuteChainTest {

    private UndoAgreeExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new UndoAgreeExecuteChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .undoProposerColor(PlayerColor.BLACK)
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteUndoAgree_UndoOneMoveByProposer() {
        // Setup: BLACK made a move at (7,7)
        GameAction move1 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .timestamp(System.currentTimeMillis())
                .build();
        game.getActionHistory().add(move1);
        game.getCurrentState().getBoard()[7][7] = 1; // BLACK
        game.getCurrentState().setTotalMoves(1);
        game.getCurrentState().setCurrentTurn(PlayerColor.WHITE);
        game.setUndoProposerColor(PlayerColor.BLACK); // BLACK wants to undo their own move

        // WHITE agrees to undo
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify move is reverted
        assertEquals(0, game.getCurrentState().getBoard()[7][7]);
        assertEquals(0, game.getCurrentState().getTotalMoves());
        assertEquals(PlayerColor.BLACK, game.getCurrentState().getCurrentTurn()); // BLACK gets to replay
        assertNull(game.getUndoProposerColor());
        assertFalse(game.getActionHistory().contains(move1));
    }

    @Test
    void testExecuteUndoAgree_UndoTwoMovesWhenOpponentMovedLast() {
        // Setup: BLACK moved at (7,7), then WHITE moved at (8,8)
        GameAction move1 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .timestamp(System.currentTimeMillis())
                .build();
        GameAction move2 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(new Position(8, 8))
                .timestamp(System.currentTimeMillis() + 1000)
                .build();
        game.getActionHistory().add(move1);
        game.getActionHistory().add(move2);
        game.getCurrentState().getBoard()[7][7] = 1; // BLACK
        game.getCurrentState().getBoard()[8][8] = 2; // WHITE
        game.getCurrentState().setTotalMoves(2);
        game.getCurrentState().setCurrentTurn(PlayerColor.BLACK);
        game.setUndoProposerColor(PlayerColor.BLACK); // BLACK wants to undo opponent's move + their own

        // WHITE agrees to undo
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify both moves are reverted
        assertEquals(0, game.getCurrentState().getBoard()[7][7]);
        assertEquals(0, game.getCurrentState().getBoard()[8][8]);
        assertEquals(0, game.getCurrentState().getTotalMoves());
        assertEquals(PlayerColor.BLACK, game.getCurrentState().getCurrentTurn()); // BLACK gets to replay
        assertNull(game.getUndoProposerColor());
        assertFalse(game.getActionHistory().contains(move1));
        assertFalse(game.getActionHistory().contains(move2));
    }

    @Test
    void testExecuteUndoAgree_ThrowsExceptionWhenNoMoves() {
        game.setActionHistory(new ArrayList<>()); // No moves

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        assertThrows(IllegalStateException.class, () -> chain.execute(game, action));
    }

    @Test
    void testExecuteUndoAgree_ThrowsExceptionWhenNotEnoughMoves() {
        // Setup: Only 1 move exists, but proposer wants to undo 2
        GameAction move1 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(new Position(7, 7))
                .timestamp(System.currentTimeMillis())
                .build();
        game.getActionHistory().add(move1);
        game.getCurrentState().getBoard()[7][7] = 2; // WHITE
        game.getCurrentState().setTotalMoves(1);
        game.setUndoProposerColor(PlayerColor.BLACK); // BLACK wants to undo opponent's move + their own (but BLACK has no move)

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        assertThrows(IllegalStateException.class, () -> chain.execute(game, action));
    }

    @Test
    void testExecuteUndoAgree_WithMultipleMoves() {
        // Setup: Multiple moves
        GameAction move1 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(5, 5))
                .timestamp(System.currentTimeMillis())
                .build();
        GameAction move2 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .position(new Position(6, 6))
                .timestamp(System.currentTimeMillis() + 1000)
                .build();
        GameAction move3 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(7, 7))
                .timestamp(System.currentTimeMillis() + 2000)
                .build();
        game.getActionHistory().add(move1);
        game.getActionHistory().add(move2);
        game.getActionHistory().add(move3);
        game.getCurrentState().getBoard()[5][5] = 1; // BLACK
        game.getCurrentState().getBoard()[6][6] = 2; // WHITE
        game.getCurrentState().getBoard()[7][7] = 1; // BLACK
        game.getCurrentState().setTotalMoves(3);
        game.getCurrentState().setCurrentTurn(PlayerColor.WHITE);
        game.setUndoProposerColor(PlayerColor.BLACK); // BLACK wants to undo their last move

        // WHITE agrees to undo
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify only the last move is reverted
        assertEquals(1, game.getCurrentState().getBoard()[5][5]);
        assertEquals(2, game.getCurrentState().getBoard()[6][6]);
        assertEquals(0, game.getCurrentState().getBoard()[7][7]); // Reverted
        assertEquals(2, game.getCurrentState().getTotalMoves());
        assertEquals(PlayerColor.BLACK, game.getCurrentState().getCurrentTurn());
        assertNull(game.getUndoProposerColor());
        assertTrue(game.getActionHistory().contains(move1));
        assertTrue(game.getActionHistory().contains(move2));
        assertFalse(game.getActionHistory().contains(move3));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.UNDO_AGREE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
