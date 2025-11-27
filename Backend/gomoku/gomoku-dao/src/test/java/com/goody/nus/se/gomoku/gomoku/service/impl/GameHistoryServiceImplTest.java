package com.goody.nus.se.gomoku.gomoku.service.impl;

import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link GameHistoryServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class GameHistoryServiceImplTest {

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @InjectMocks
    private GameHistoryServiceImpl gameHistoryService;

    private GameDocument testGameDoc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testGameDoc = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();
    }

    // ==================== ArchiveGame Tests ====================

    @Test
    void archiveGame_withValidGame_shouldSaveAndReturn() {
        // Given
        Integer gameNumber = 1;
        String endReason = "Win by five in a row";

        GameHistoryDocument expectedHistory = GameHistoryDocument.fromGameDocument(testGameDoc, gameNumber, endReason);
        expectedHistory.setId("history123");

        when(gameHistoryRepository.save(any(GameHistoryDocument.class))).thenReturn(expectedHistory);

        // When
        GameHistoryDocument result = gameHistoryService.archiveGame(testGameDoc, gameNumber, endReason);

        // Then
        assertNotNull(result);
        assertEquals("history123", result.getId());
        assertEquals(testGameDoc.getRoomId(), result.getRoomId());
        assertEquals(gameNumber, result.getGameNumber());
        assertEquals(endReason, result.getEndReason());

        ArgumentCaptor<GameHistoryDocument> captor = ArgumentCaptor.forClass(GameHistoryDocument.class);
        verify(gameHistoryRepository, times(1)).save(captor.capture());

        GameHistoryDocument saved = captor.getValue();
        assertEquals(testGameDoc.getRoomId(), saved.getRoomId());
        assertEquals(gameNumber, saved.getGameNumber());
        assertEquals(endReason, saved.getEndReason());
    }

    @Test
    void archiveGame_withGameNumber_shouldStoreCorrectNumber() {
        // Given
        Integer gameNumber = 5;
        String endReason = "Surrender";

        GameHistoryDocument mockHistory = new GameHistoryDocument();
        mockHistory.setGameNumber(gameNumber);

        when(gameHistoryRepository.save(any(GameHistoryDocument.class))).thenReturn(mockHistory);

        // When
        GameHistoryDocument result = gameHistoryService.archiveGame(testGameDoc, gameNumber, endReason);

        // Then
        assertEquals(gameNumber, result.getGameNumber());
        verify(gameHistoryRepository, times(1)).save(any(GameHistoryDocument.class));
    }

    @Test
    void archiveGame_withDifferentEndReasons_shouldStoreThem() {
        // Given
        String[] endReasons = {"Win by five", "Timeout", "Surrender", "Draw agreed"};

        for (String endReason : endReasons) {
            GameHistoryDocument mockHistory = new GameHistoryDocument();
            mockHistory.setEndReason(endReason);

            when(gameHistoryRepository.save(any(GameHistoryDocument.class))).thenReturn(mockHistory);

            // When
            GameHistoryDocument result = gameHistoryService.archiveGame(testGameDoc, 1, endReason);

            // Then
            assertEquals(endReason, result.getEndReason());
        }

        verify(gameHistoryRepository, times(endReasons.length)).save(any(GameHistoryDocument.class));
    }

    // ==================== GetHistoryByRoomId Tests ====================

    @Test
    void getHistoryByRoomId_withExistingHistory_shouldReturnList() {
        // Given
        Long roomId = 1L;
        List<GameHistoryDocument> mockHistory = Arrays.asList(
                createMockHistory(roomId, 1, "Win"),
                createMockHistory(roomId, 2, "Draw"),
                createMockHistory(roomId, 3, "Surrender")
        );

        when(gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(roomId)).thenReturn(mockHistory);

        // When
        List<GameHistoryDocument> result = gameHistoryService.getHistoryByRoomId(roomId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getGameNumber());
        assertEquals(2, result.get(1).getGameNumber());
        assertEquals(3, result.get(2).getGameNumber());

        verify(gameHistoryRepository, times(1)).findByRoomIdOrderByGameNumberAsc(roomId);
    }

    @Test
    void getHistoryByRoomId_withNoHistory_shouldReturnEmptyList() {
        // Given
        Long roomId = 999L;
        when(gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(roomId)).thenReturn(Arrays.asList());

        // When
        List<GameHistoryDocument> result = gameHistoryService.getHistoryByRoomId(roomId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameHistoryRepository, times(1)).findByRoomIdOrderByGameNumberAsc(roomId);
    }

    @Test
    void getHistoryByRoomId_shouldReturnOrderedByGameNumber() {
        // Given
        Long roomId = 2L;
        List<GameHistoryDocument> mockHistory = Arrays.asList(
                createMockHistory(roomId, 1, "Win"),
                createMockHistory(roomId, 2, "Win"),
                createMockHistory(roomId, 3, "Draw")
        );

        when(gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(roomId)).thenReturn(mockHistory);

        // When
        List<GameHistoryDocument> result = gameHistoryService.getHistoryByRoomId(roomId);

        // Then
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getGameNumber() < result.get(i + 1).getGameNumber());
        }

        verify(gameHistoryRepository, times(1)).findByRoomIdOrderByGameNumberAsc(roomId);
    }

    // ==================== GetGameByRoomIdAndNumber Tests ====================

    @Test
    void getGameByRoomIdAndNumber_withExistingGame_shouldReturnOptional() {
        // Given
        Long roomId = 1L;
        Integer gameNumber = 2;
        GameHistoryDocument mockHistory = createMockHistory(roomId, gameNumber, "Win");

        when(gameHistoryRepository.findByRoomIdAndGameNumber(roomId, gameNumber))
                .thenReturn(Optional.of(mockHistory));

        // When
        Optional<GameHistoryDocument> result = gameHistoryService.getGameByRoomIdAndNumber(roomId, gameNumber);

        // Then
        assertTrue(result.isPresent());
        assertEquals(roomId, result.get().getRoomId());
        assertEquals(gameNumber, result.get().getGameNumber());

        verify(gameHistoryRepository, times(1)).findByRoomIdAndGameNumber(roomId, gameNumber);
    }

    @Test
    void getGameByRoomIdAndNumber_withNonExistingGame_shouldReturnEmpty() {
        // Given
        Long roomId = 1L;
        Integer gameNumber = 999;

        when(gameHistoryRepository.findByRoomIdAndGameNumber(roomId, gameNumber))
                .thenReturn(Optional.empty());

        // When
        Optional<GameHistoryDocument> result = gameHistoryService.getGameByRoomIdAndNumber(roomId, gameNumber);

        // Then
        assertFalse(result.isPresent());
        verify(gameHistoryRepository, times(1)).findByRoomIdAndGameNumber(roomId, gameNumber);
    }

    @Test
    void getGameByRoomIdAndNumber_withDifferentNumbers_shouldReturnCorrectGame() {
        // Given
        Long roomId = 3L;

        for (int gameNum = 1; gameNum <= 5; gameNum++) {
            GameHistoryDocument mockHistory = createMockHistory(roomId, gameNum, "Win");
            when(gameHistoryRepository.findByRoomIdAndGameNumber(roomId, gameNum))
                    .thenReturn(Optional.of(mockHistory));

            // When
            Optional<GameHistoryDocument> result = gameHistoryService.getGameByRoomIdAndNumber(roomId, gameNum);

            // Then
            assertTrue(result.isPresent());
            assertEquals(gameNum, result.get().getGameNumber());
        }

        verify(gameHistoryRepository, times(5)).findByRoomIdAndGameNumber(eq(roomId), anyInt());
    }

    // ==================== CountGamesByRoomId Tests ====================

    @Test
    void countGamesByRoomId_withExistingGames_shouldReturnCount() {
        // Given
        Long roomId = 1L;
        when(gameHistoryRepository.countByRoomId(roomId)).thenReturn(5L);

        // When
        long count = gameHistoryService.countGamesByRoomId(roomId);

        // Then
        assertEquals(5L, count);
        verify(gameHistoryRepository, times(1)).countByRoomId(roomId);
    }

    @Test
    void countGamesByRoomId_withNoGames_shouldReturnZero() {
        // Given
        Long roomId = 999L;
        when(gameHistoryRepository.countByRoomId(roomId)).thenReturn(0L);

        // When
        long count = gameHistoryService.countGamesByRoomId(roomId);

        // Then
        assertEquals(0L, count);
        verify(gameHistoryRepository, times(1)).countByRoomId(roomId);
    }

    @Test
    void countGamesByRoomId_withDifferentRooms_shouldReturnCorrectCounts() {
        // Given
        when(gameHistoryRepository.countByRoomId(1L)).thenReturn(3L);
        when(gameHistoryRepository.countByRoomId(2L)).thenReturn(7L);
        when(gameHistoryRepository.countByRoomId(3L)).thenReturn(1L);

        // When & Then
        assertEquals(3L, gameHistoryService.countGamesByRoomId(1L));
        assertEquals(7L, gameHistoryService.countGamesByRoomId(2L));
        assertEquals(1L, gameHistoryService.countGamesByRoomId(3L));

        verify(gameHistoryRepository, times(1)).countByRoomId(1L);
        verify(gameHistoryRepository, times(1)).countByRoomId(2L);
        verify(gameHistoryRepository, times(1)).countByRoomId(3L);
    }

    // ==================== GetGamesByPlayerId Tests ====================

    @Test
    void getGamesByPlayerId_withExistingGames_shouldReturnList() {
        // Given
        Long playerId = 100L;
        List<GameHistoryDocument> mockGames = Arrays.asList(
                createMockHistory(1L, 1, "Win"),
                createMockHistory(2L, 1, "Loss"),
                createMockHistory(3L, 1, "Draw")
        );

        when(gameHistoryRepository.findByBlackPlayerIdOrWhitePlayerId(playerId, playerId))
                .thenReturn(mockGames);

        // When
        List<GameHistoryDocument> result = gameHistoryService.getGamesByPlayerId(playerId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(gameHistoryRepository, times(1)).findByBlackPlayerIdOrWhitePlayerId(playerId, playerId);
    }

    @Test
    void getGamesByPlayerId_withNoGames_shouldReturnEmptyList() {
        // Given
        Long playerId = 999L;
        when(gameHistoryRepository.findByBlackPlayerIdOrWhitePlayerId(playerId, playerId))
                .thenReturn(Arrays.asList());

        // When
        List<GameHistoryDocument> result = gameHistoryService.getGamesByPlayerId(playerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameHistoryRepository, times(1)).findByBlackPlayerIdOrWhitePlayerId(playerId, playerId);
    }

    @Test
    void getGamesByPlayerId_shouldPassPlayerIdTwice() {
        // Given - Testing MongoDB OR query pattern
        Long playerId = 150L;
        List<GameHistoryDocument> mockGames = Arrays.asList(createMockHistory(1L, 1, "Win"));

        when(gameHistoryRepository.findByBlackPlayerIdOrWhitePlayerId(playerId, playerId))
                .thenReturn(mockGames);

        // When
        gameHistoryService.getGamesByPlayerId(playerId);

        // Then - Verify playerId is passed twice (for blackPlayerId OR whitePlayerId)
        verify(gameHistoryRepository, times(1)).findByBlackPlayerIdOrWhitePlayerId(eq(playerId), eq(playerId));
    }

    // ==================== GetWinsByPlayerId Tests ====================

    @Test
    void getWinsByPlayerId_withExistingWins_shouldReturnList() {
        // Given
        Long playerId = 100L;
        List<GameHistoryDocument> mockWins = Arrays.asList(
                createMockHistory(1L, 1, "Win"),
                createMockHistory(2L, 1, "Win")
        );

        when(gameHistoryRepository.findByWinnerId(playerId)).thenReturn(mockWins);

        // When
        List<GameHistoryDocument> result = gameHistoryService.getWinsByPlayerId(playerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gameHistoryRepository, times(1)).findByWinnerId(playerId);
    }

    @Test
    void getWinsByPlayerId_withNoWins_shouldReturnEmptyList() {
        // Given
        Long playerId = 999L;
        when(gameHistoryRepository.findByWinnerId(playerId)).thenReturn(Arrays.asList());

        // When
        List<GameHistoryDocument> result = gameHistoryService.getWinsByPlayerId(playerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameHistoryRepository, times(1)).findByWinnerId(playerId);
    }

    @Test
    void getWinsByPlayerId_withDifferentPlayers_shouldReturnCorrectWins() {
        // Given
        Long player1 = 100L;
        Long player2 = 200L;

        when(gameHistoryRepository.findByWinnerId(player1)).thenReturn(Arrays.asList(
                createMockHistory(1L, 1, "Win"),
                createMockHistory(1L, 2, "Win")
        ));
        when(gameHistoryRepository.findByWinnerId(player2)).thenReturn(Arrays.asList(
                createMockHistory(1L, 3, "Win")
        ));

        // When & Then
        assertEquals(2, gameHistoryService.getWinsByPlayerId(player1).size());
        assertEquals(1, gameHistoryService.getWinsByPlayerId(player2).size());

        verify(gameHistoryRepository, times(1)).findByWinnerId(player1);
        verify(gameHistoryRepository, times(1)).findByWinnerId(player2);
    }

    // ==================== DeleteHistoryByRoomId Tests ====================

    @Test
    void deleteHistoryByRoomId_withExistingHistory_shouldReturnDeletedCount() {
        // Given
        Long roomId = 1L;
        when(gameHistoryRepository.deleteByRoomId(roomId)).thenReturn(5L);

        // When
        long deleted = gameHistoryService.deleteHistoryByRoomId(roomId);

        // Then
        assertEquals(5L, deleted);
        verify(gameHistoryRepository, times(1)).deleteByRoomId(roomId);
    }

    @Test
    void deleteHistoryByRoomId_withNoHistory_shouldReturnZero() {
        // Given
        Long roomId = 999L;
        when(gameHistoryRepository.deleteByRoomId(roomId)).thenReturn(0L);

        // When
        long deleted = gameHistoryService.deleteHistoryByRoomId(roomId);

        // Then
        assertEquals(0L, deleted);
        verify(gameHistoryRepository, times(1)).deleteByRoomId(roomId);
    }

    @Test
    void deleteHistoryByRoomId_withMultipleRooms_shouldDeleteCorrectly() {
        // Given
        when(gameHistoryRepository.deleteByRoomId(1L)).thenReturn(3L);
        when(gameHistoryRepository.deleteByRoomId(2L)).thenReturn(7L);
        when(gameHistoryRepository.deleteByRoomId(3L)).thenReturn(1L);

        // When & Then
        assertEquals(3L, gameHistoryService.deleteHistoryByRoomId(1L));
        assertEquals(7L, gameHistoryService.deleteHistoryByRoomId(2L));
        assertEquals(1L, gameHistoryService.deleteHistoryByRoomId(3L));

        verify(gameHistoryRepository, times(1)).deleteByRoomId(1L);
        verify(gameHistoryRepository, times(1)).deleteByRoomId(2L);
        verify(gameHistoryRepository, times(1)).deleteByRoomId(3L);
    }

    // ==================== Helper Methods ====================

    private GameHistoryDocument createMockHistory(Long roomId, Integer gameNumber, String endReason) {
        GameHistoryDocument history = new GameHistoryDocument();
        history.setId("history_" + roomId + "_" + gameNumber);
        history.setRoomId(roomId);
        history.setGameNumber(gameNumber);
        history.setEndReason(endReason);
        history.setBlackPlayerId(100L);
        history.setWhitePlayerId(200L);
        return history;
    }
}
