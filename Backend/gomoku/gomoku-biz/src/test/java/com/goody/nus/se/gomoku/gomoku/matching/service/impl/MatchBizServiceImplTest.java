package com.goody.nus.se.gomoku.gomoku.matching.service.impl;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.Service.RoomCodeService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IRoomStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for MatchBizServiceImpl
 *
 * @author Claude
 * @version 1.0
 */
class MatchBizServiceImplTest {

    @Mock
    private IMatchService matchService;

    @Mock
    private RoomCodeService roomCodeService;

    @Mock
    private IRoomStateService roomStateService;

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private MatchBizServiceImpl matchBizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMatchAndSave_NotMatched() {
        String playerId = "player123";
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("queued");
        mockResponse.setRoomCode(null);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("queued", response.getStatus());
        verify(matchService).match(request, playerId);
        verify(roomCodeService, never()).saveRoomRecord(any(), any(), any(), anyByte());
        verify(roomStateService, never()).initializeGameState(any(), any(), any(), any());
    }

    @Test
    void testMatchAndSave_MatchedButNoRoomCode() {
        String playerId = "player123";
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(null);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        verify(matchService).match(request, playerId);
        verify(roomCodeService, never()).saveRoomRecord(any(), any(), any(), anyByte());
        verify(roomStateService, never()).initializeGameState(any(), any(), any(), any());
    }

    @Test
    void testMatchAndSave_InvalidPlayersList_Null() {
        String playerId = "player123";
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode("ABCD1234");
        mockResponse.setPlayers(null);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        verify(matchService).match(request, playerId);
        verify(roomCodeService, never()).saveRoomRecord(any(), any(), any(), anyByte());
        verify(roomStateService, never()).initializeGameState(any(), any(), any(), any());
    }

    @Test
    void testMatchAndSave_InvalidPlayersList_WrongSize() {
        String playerId = "player123";
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode("ABCD1234");
        mockResponse.setPlayers(Collections.singletonList("player123"));

        when(matchService.match(request, playerId)).thenReturn(mockResponse);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        verify(matchService).match(request, playerId);
        verify(roomCodeService, never()).saveRoomRecord(any(), any(), any(), anyByte());
        verify(roomStateService, never()).initializeGameState(any(), any(), any(), any());
    }

    @Test
    void testMatchAndSave_CasualMode_Success() {
        String playerId = "player123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        List<String> players = Arrays.asList("100", "200");

        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(roomCode);
        mockResponse.setPlayers(players);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(null);
        when(roomCodeService.saveRoomRecord(eq(roomCode), eq(100L), eq(200L), eq((byte) 0))).thenReturn(roomId);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        verify(matchService).match(request, playerId);
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(roomCodeService).saveRoomRecord(roomCode, 100L, 200L, (byte) 0);
        verify(roomStateService).initializeGameState(roomId, 100L, 200L, "CASUAL");
    }

    @Test
    void testMatchAndSave_RankedMode_Success() {
        String playerId = "player123";
        String roomCode = "ABCD1234";
        Long roomId = 2L;
        List<String> players = Arrays.asList("100", "200");

        MatchRequest request = new MatchRequest();
        request.setMode("ranked");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(roomCode);
        mockResponse.setPlayers(players);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(null);
        when(roomCodeService.saveRoomRecord(eq(roomCode), eq(100L), eq(200L), eq((byte) 1))).thenReturn(roomId);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        verify(matchService).match(request, playerId);
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(roomCodeService).saveRoomRecord(roomCode, 100L, 200L, (byte) 1);
        verify(roomStateService).initializeGameState(roomId, 100L, 200L, "RANKED");
    }

    @Test
    void testMatchAndSave_RoomAlreadyExists() {
        String playerId = "player123";
        String roomCode = "ABCD1234";
        Long existingRoomId = 3L;
        List<String> players = Arrays.asList("100", "200");

        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(roomCode);
        mockResponse.setPlayers(players);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(existingRoomId);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(existingRoomId, response.getRoomId());
        verify(matchService).match(request, playerId);
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(roomCodeService, never()).saveRoomRecord(any(), any(), any(), anyByte());
        verify(roomStateService).initializeGameState(existingRoomId, 100L, 200L, "CASUAL");
    }

    @Test
    void testMatchAndSave_CasualMode_CaseInsensitive() {
        String playerId = "player123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        List<String> players = Arrays.asList("100", "200");

        MatchRequest request = new MatchRequest();
        request.setMode("CASUAL"); // Uppercase

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(roomCode);
        mockResponse.setPlayers(players);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(null);
        when(roomCodeService.saveRoomRecord(eq(roomCode), eq(100L), eq(200L), eq((byte) 0))).thenReturn(roomId);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        verify(roomStateService).initializeGameState(roomId, 100L, 200L, "CASUAL");
    }

    @Test
    void testMatchAndSave_UnknownMode_TreatedAsRanked() {
        String playerId = "player123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        List<String> players = Arrays.asList("100", "200");

        MatchRequest request = new MatchRequest();
        request.setMode("unknown"); // Unknown mode

        MatchResponse mockResponse = new MatchResponse();
        mockResponse.setStatus("matched");
        mockResponse.setRoomCode(roomCode);
        mockResponse.setPlayers(players);

        when(matchService.match(request, playerId)).thenReturn(mockResponse);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(null);
        when(roomCodeService.saveRoomRecord(eq(roomCode), eq(100L), eq(200L), eq((byte) 1))).thenReturn(roomId);

        MatchResponse response = matchBizService.matchAndSave(request, playerId);

        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        verify(roomStateService).initializeGameState(roomId, 100L, 200L, "RANKED");
    }

    @Test
    void testCancelMatch_Success() {
        String playerId = "player123";

        CancelMatchResponse mockResponse = new CancelMatchResponse();
        mockResponse.setStatus("success");

        when(matchService.cancelMatch(playerId)).thenReturn(mockResponse);

        CancelMatchResponse response = matchBizService.cancelMatch(playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(matchService).cancelMatch(playerId);
    }

    @Test
    void testCancelMatch_NotInQueue() {
        String playerId = "player123";

        CancelMatchResponse mockResponse = new CancelMatchResponse();
        mockResponse.setStatus("not_in_queue");

        when(matchService.cancelMatch(playerId)).thenReturn(mockResponse);

        CancelMatchResponse response = matchBizService.cancelMatch(playerId);

        assertNotNull(response);
        assertEquals("not_in_queue", response.getStatus());
        verify(matchService).cancelMatch(playerId);
    }
}
