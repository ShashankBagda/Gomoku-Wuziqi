package com.goody.nus.se.gomoku.gomoku.mongo.entity;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for GameHistoryDocument
 *
 * @author Claude
 * @version 1.0
 */
class GameHistoryDocumentTest {

    @Test
    @DisplayName("Should create history document from game document with black winner")
    void testFromGameDocumentBlackWins() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1); // Black wins
        state.setTotalMoves(45);

        List<GameAction> actions = new ArrayList<>();
        actions.add(GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build());

        GameDocument gameDoc = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(actions)
                .createTime(1000L)
                .build();

        long beforeCreate = System.currentTimeMillis();
        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 1, "WIN");
        long afterCreate = System.currentTimeMillis();

        assertEquals(1L, history.getRoomId());
        assertEquals(1, history.getGameNumber());
        assertEquals(100L, history.getBlackPlayerId());
        assertEquals(200L, history.getWhitePlayerId());
        assertEquals(state, history.getFinalState());
        assertEquals(actions, history.getActionHistory());
        assertEquals(1000L, history.getStartTime());
        assertTrue(history.getEndTime() >= beforeCreate && history.getEndTime() <= afterCreate);
        assertEquals(100L, history.getWinnerId()); // Black player ID
        assertEquals(45, history.getTotalMoves());
        assertEquals("WIN", history.getEndReason());
    }

    @Test
    @DisplayName("Should create history document from game document with white winner")
    void testFromGameDocumentWhiteWins() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(2); // White wins
        state.setTotalMoves(50);

        GameDocument gameDoc = GameDocument.builder()
                .roomId(2L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .createTime(2000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 2, "SURRENDER");

        assertEquals(200L, history.getWinnerId()); // White player ID
        assertEquals("SURRENDER", history.getEndReason());
    }

    @Test
    @DisplayName("Should create history document with draw (winner=0)")
    void testFromGameDocumentDraw() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(0); // Draw
        state.setTotalMoves(225); // Full board

        GameDocument gameDoc = GameDocument.builder()
                .roomId(3L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .createTime(3000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 3, "DRAW");

        assertNull(history.getWinnerId()); // No winner for draw
        assertEquals("DRAW", history.getEndReason());
        assertEquals(225, history.getTotalMoves());
    }

    @Test
    @DisplayName("Should handle ongoing game (winner=-1)")
    void testFromGameDocumentOngoing() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(-1); // Ongoing
        state.setTotalMoves(20);

        GameDocument gameDoc = GameDocument.builder()
                .roomId(4L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .createTime(4000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 4, "ONGOING");

        assertNull(history.getWinnerId());
        assertEquals("ONGOING", history.getEndReason());
    }

    @Test
    @DisplayName("Should handle null winner")
    void testFromGameDocumentNullWinner() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(null);

        GameDocument gameDoc = GameDocument.builder()
                .roomId(5L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .createTime(5000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 5, "UNKNOWN");

        assertNull(history.getWinnerId());
    }

    @Test
    @DisplayName("Should handle null currentState")
    void testFromGameDocumentNullState() {
        GameDocument gameDoc = GameDocument.builder()
                .roomId(6L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(null)
                .actionHistory(new ArrayList<>())
                .createTime(6000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 6, "ERROR");

        assertNull(history.getWinnerId());
        assertEquals(0, history.getTotalMoves());
    }

    @Test
    @DisplayName("Should handle invalid winner value")
    void testFromGameDocumentInvalidWinner() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(99); // Invalid value

        GameDocument gameDoc = GameDocument.builder()
                .roomId(7L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .createTime(7000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 7, "UNKNOWN");

        assertNull(history.getWinnerId());
    }

    @Test
    @DisplayName("Should calculate game duration correctly")
    void testGetDuration() {
        GameHistoryDocument history = GameHistoryDocument.builder()
                .startTime(1000L)
                .endTime(6000L)
                .build();

        assertEquals(5000L, history.getDuration());
    }

    @Test
    @DisplayName("Should return 0 duration when startTime is null")
    void testGetDurationNullStartTime() {
        GameHistoryDocument history = GameHistoryDocument.builder()
                .startTime(null)
                .endTime(6000L)
                .build();

        assertEquals(0L, history.getDuration());
    }

    @Test
    @DisplayName("Should return 0 duration when endTime is null")
    void testGetDurationNullEndTime() {
        GameHistoryDocument history = GameHistoryDocument.builder()
                .startTime(1000L)
                .endTime(null)
                .build();

        assertEquals(0L, history.getDuration());
    }

    @Test
    @DisplayName("Should return 0 duration when both times are null")
    void testGetDurationBothNull() {
        GameHistoryDocument history = GameHistoryDocument.builder()
                .startTime(null)
                .endTime(null)
                .build();

        assertEquals(0L, history.getDuration());
    }

    @Test
    @DisplayName("Should handle zero duration")
    void testGetDurationZero() {
        GameHistoryDocument history = GameHistoryDocument.builder()
                .startTime(1000L)
                .endTime(1000L)
                .build();

        assertEquals(0L, history.getDuration());
    }

    @Test
    @DisplayName("Should preserve all game details in history")
    void testFromGameDocumentPreservesAllDetails() {
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1);
        state.setTotalMoves(30);

        List<GameAction> actions = new ArrayList<>();
        actions.add(GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build());
        actions.add(GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build());

        GameDocument gameDoc = GameDocument.builder()
                .roomId(123L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .currentState(state)
                .actionHistory(actions)
                .createTime(10000L)
                .build();

        GameHistoryDocument history = GameHistoryDocument.fromGameDocument(gameDoc, 5, "WIN");

        // Verify all details are preserved
        assertEquals(123L, history.getRoomId());
        assertEquals(5, history.getGameNumber());
        assertEquals(100L, history.getBlackPlayerId());
        assertEquals(200L, history.getWhitePlayerId());
        assertSame(state, history.getFinalState());
        assertSame(actions, history.getActionHistory());
        assertEquals(2, history.getActionHistory().size());
        assertEquals(10000L, history.getStartTime());
        assertEquals(100L, history.getWinnerId());
        assertEquals(30, history.getTotalMoves());
        assertEquals("WIN", history.getEndReason());
    }
}
