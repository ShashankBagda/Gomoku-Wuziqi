package com.goody.nus.se.gomoku.gomoku.mongo.entity;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for GameDocument
 *
 * @author Claude
 * @version 1.0
 */
class GameDocumentTest {

    @Test
    @DisplayName("Should create new game with black player when timestamp is even")
    void testCreateNewGameWithBlackPlayer() {
        Long roomId = 123L;
        Long playerId = 456L;
        String modeType = "RANKED";

        // Set system time to even value to ensure black assignment
        GameDocument game = GameDocument.createNewGameWithRandomBlack(roomId, playerId, modeType);

        assertNotNull(game);
        assertEquals(roomId, game.getRoomId());
        assertEquals(modeType, game.getModeType());
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(0L, game.getVersion());
        assertNotNull(game.getCreateTime());
        assertNotNull(game.getUpdateTime());
        assertNotNull(game.getCurrentState());
        assertNotNull(game.getActionHistory());
        assertTrue(game.getActionHistory().isEmpty());
        assertNull(game.getDrawProposerColor());
        assertNull(game.getUndoProposerColor());
        assertFalse(game.getBlackReady());
        assertFalse(game.getWhiteReady());

        // Verify player is assigned to either black or white
        assertTrue((game.getBlackPlayerId() != null && game.getBlackPlayerId().equals(playerId) && game.getWhitePlayerId() == null) ||
                (game.getWhitePlayerId() != null && game.getWhitePlayerId().equals(playerId) && game.getBlackPlayerId() == null));
    }

    @Test
    @DisplayName("Should add action to history")
    void testAddActionToHistory() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .actionHistory(new ArrayList<>())
                .build();

        GameAction action1 = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        GameAction action2 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        game.addActionToHistory(action1);
        assertEquals(1, game.getActionHistory().size());
        assertEquals(action1, game.getActionHistory().get(0));

        game.addActionToHistory(action2);
        assertEquals(2, game.getActionHistory().size());
        assertEquals(action2, game.getActionHistory().get(1));
    }

    @Test
    @DisplayName("Should initialize action history if null")
    void testAddActionToHistoryNullList() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .actionHistory(null)
                .build();

        assertNull(game.getActionHistory());

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        game.addActionToHistory(action);

        assertNotNull(game.getActionHistory());
        assertEquals(1, game.getActionHistory().size());
        assertEquals(action, game.getActionHistory().get(0));
    }

    @Test
    @DisplayName("Should clear draw proposal")
    void testClearDrawProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .drawProposerColor(PlayerColor.BLACK)
                .build();

        assertTrue(game.hasPendingDrawProposal());
        assertEquals(PlayerColor.BLACK, game.getDrawProposerColor());

        game.clearDrawProposal();

        assertFalse(game.hasPendingDrawProposal());
        assertNull(game.getDrawProposerColor());
    }

    @Test
    @DisplayName("Should check pending draw proposal")
    void testHasPendingDrawProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .drawProposerColor(null)
                .build();

        assertFalse(game.hasPendingDrawProposal());

        game.setDrawProposerColor(PlayerColor.WHITE);
        assertTrue(game.hasPendingDrawProposal());
    }

    @Test
    @DisplayName("Should clear undo proposal")
    void testClearUndoProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .undoProposerColor(PlayerColor.WHITE)
                .build();

        assertTrue(game.hasPendingUndoProposal());
        assertEquals(PlayerColor.WHITE, game.getUndoProposerColor());

        game.clearUndoProposal();

        assertFalse(game.hasPendingUndoProposal());
        assertNull(game.getUndoProposerColor());
    }

    @Test
    @DisplayName("Should check pending undo proposal")
    void testHasPendingUndoProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .undoProposerColor(null)
                .build();

        assertFalse(game.hasPendingUndoProposal());

        game.setUndoProposerColor(PlayerColor.BLACK);
        assertTrue(game.hasPendingUndoProposal());
    }

    @Test
    @DisplayName("Should clear restart proposal")
    void testClearRestartProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .restartProposerColor(PlayerColor.BLACK)
                .build();

        assertTrue(game.hasPendingRestartProposal());
        assertEquals(PlayerColor.BLACK, game.getRestartProposerColor());

        game.clearRestartProposal();

        assertFalse(game.hasPendingRestartProposal());
        assertNull(game.getRestartProposerColor());
    }

    @Test
    @DisplayName("Should check pending restart proposal")
    void testHasPendingRestartProposal() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .restartProposerColor(null)
                .build();

        assertFalse(game.hasPendingRestartProposal());

        game.setRestartProposerColor(PlayerColor.WHITE);
        assertTrue(game.hasPendingRestartProposal());
    }

    @Test
    @DisplayName("Should reset for new game and swap colors")
    void testResetForNewGame() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1);
        state.setTotalMoves(50);

        GameAction lastAction = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .blackReady(true)
                .whiteReady(true)
                .currentState(state)
                .lastAction(lastAction)
                .actionHistory(new ArrayList<>())
                .status(GameStatus.FINISHED)
                .drawProposerColor(PlayerColor.BLACK)
                .undoProposerColor(PlayerColor.WHITE)
                .restartProposerColor(PlayerColor.BLACK)
                .gameCount(1)
                .createTime(1000L)
                .build();

        long beforeReset = System.currentTimeMillis();
        game.resetForNewGame();
        long afterReset = System.currentTimeMillis();

        // Verify colors were swapped
        assertEquals(200L, game.getBlackPlayerId());
        assertEquals(100L, game.getWhitePlayerId());

        // Verify state was reset
        assertNotNull(game.getCurrentState());
        assertEquals(-1, game.getCurrentState().getWinner());
        assertEquals(0, game.getCurrentState().getTotalMoves());

        // Verify ready status was reset
        assertFalse(game.getBlackReady());
        assertFalse(game.getWhiteReady());

        // Verify game status was reset
        assertEquals(GameStatus.WAITING, game.getStatus());

        // Verify all proposals were cleared
        assertNull(game.getDrawProposerColor());
        assertNull(game.getUndoProposerColor());
        assertNull(game.getRestartProposerColor());

        // Verify last action and history were reset
        assertNull(game.getLastAction());
        assertNotNull(game.getActionHistory());
        assertTrue(game.getActionHistory().isEmpty());

        // Verify game count was incremented
        assertEquals(2, game.getGameCount());

        // Verify timestamps
        assertEquals(1000L, game.getCreateTime()); // Original create time preserved
        assertTrue(game.getUpdateTime() >= beforeReset && game.getUpdateTime() <= afterReset);
    }

    @Test
    @DisplayName("Should handle null game count in reset")
    void testResetForNewGameWithNullGameCount() {
        GameDocument game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .gameCount(null)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .build();

        game.resetForNewGame();

        // Should set gameCount to 2 (starts at 1, then increments)
        assertEquals(2, game.getGameCount());
    }

    @Test
    @DisplayName("Should preserve roomId in reset")
    void testResetForNewGamePreservesRoomId() {
        GameDocument game = GameDocument.builder()
                .roomId(999L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .gameCount(5)
                .build();

        game.resetForNewGame();

        assertEquals(999L, game.getRoomId());
        assertEquals(6, game.getGameCount());
    }
}
