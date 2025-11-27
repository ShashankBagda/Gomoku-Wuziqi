package com.goody.nus.se.gomoku.gomoku.matching.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import com.goody.nus.se.gomoku.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link MatchServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class MatchServiceImplTest {

    @Mock
    private RedisService redisService;

    @Mock
    private RoomCodeDao roomCodeDao;

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private static final String CASUAL_QUEUE = "match:casual";
    private static final String RANKED_QUEUE = "match:ranked";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void match_CasualMode_QueueKeySelection() {
        // Given
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(CASUAL_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        verify(redisService).lPush(CASUAL_QUEUE, playerId);
        verify(redisService).expire(CASUAL_QUEUE, 3, TimeUnit.MINUTES);
    }

    @Test
    void match_RankedMode_QueueKeySelection() {
        // Given
        MatchRequest request = new MatchRequest();
        request.setMode("ranked");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(RANKED_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        verify(redisService).lPush(RANKED_QUEUE, playerId);
        verify(redisService).expire(RANKED_QUEUE, 3, TimeUnit.MINUTES);
    }

    @Test
    void match_PlayerInMatchedRoom_TwoPlayers() {
        // Given - Player already in room with 2 players (matched)
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";
        String roomCode = "ABC123";
        List<String> players = Arrays.asList("100", "200");
        Long roomId = 1L;

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(roomCode);
        when(roomCodeDao.getPlayersByRoom(roomCode)).thenReturn(players);
        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomCode, response.getRoomCode());
        assertEquals(roomId, response.getRoomId());
        assertEquals(players, response.getPlayers());
        assertTrue(response.getMessage().contains("Already matched"));
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(roomCodeDao).getPlayersByRoom(roomCode);
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(redisService, never()).lPush(anyString(), anyString());
    }

    @Test
    void match_PlayerInWaitingRoom_OnePlayer() {
        // Given - Player already in room with 1 player (waiting)
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";
        String roomCode = "ABC123";
        List<String> players = Collections.singletonList("100");

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(roomCode);
        when(roomCodeDao.getPlayersByRoom(roomCode)).thenReturn(players);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertEquals(roomCode, response.getRoomCode());
        assertEquals(players, response.getPlayers());
        assertTrue(response.getMessage().contains("Waiting for another player"));
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(roomCodeDao).getPlayersByRoom(roomCode);
        verify(gameRoomService, never()).findRoomIdByRoomCode(anyString());
        verify(redisService, never()).lPush(anyString(), anyString());
    }

    @Test
    void match_PlayerInWaitingRoom_NullPlayers() {
        // Given - Player in room but players list is null
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";
        String roomCode = "ABC123";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(roomCode);
        when(roomCodeDao.getPlayersByRoom(roomCode)).thenReturn(null);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertEquals(roomCode, response.getRoomCode());
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(roomCodeDao).getPlayersByRoom(roomCode);
        verify(gameRoomService, never()).findRoomIdByRoomCode(anyString());
    }

    @Test
    void match_PlayerInDifferentQueue_ThrowsException() {
        // Given - Player in casual queue tries to join ranked
        MatchRequest request = new MatchRequest();
        request.setMode("ranked");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BizException.class, () -> matchService.match(request, playerId));
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(redisService, never()).lPush(anyString(), anyString());
    }

    @Test
    void match_PlayerInSameQueue_AlreadyExists() {
        // Given - Player already in the queue they're trying to join (idempotency)
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertTrue(response.getMessage().contains("already in the casual queue"));
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(redisService, atLeastOnce()).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService, never()).lPush(anyString(), anyString());
    }

    @Test
    void match_NewPlayer_QueueSizeOne_Waiting() {
        // Given - New player joins empty queue
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(CASUAL_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertTrue(response.getMessage().contains("Waiting for another player"));
        verify(roomCodeDao).findRoomCodeByPlayerId(playerId);
        verify(redisService).lPush(CASUAL_QUEUE, playerId);
        verify(redisService, atLeastOnce()).lSize(CASUAL_QUEUE);
        verify(redisService).expire(CASUAL_QUEUE, 3, TimeUnit.MINUTES);
    }

    @Test
    void match_SecondPlayer_QueueSizeTwo_Matched() {
        // Given - Second player joins, queue has 2 players, should match
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        String playerId = "200";
        List<Object> matchedPlayers = Arrays.asList("100", "200");

        @SuppressWarnings("unchecked")
        RList<Object> mockRList = mock(RList.class);

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(CASUAL_QUEUE)).thenReturn(2);
        when(redisService.lRange(CASUAL_QUEUE, 0, 1)).thenReturn(matchedPlayers);
        when(redisService.getList(CASUAL_QUEUE)).thenReturn(mockRList);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertNotNull(response.getRoomCode());
        assertEquals(6, response.getRoomCode().length());
        assertEquals(2, response.getPlayers().size());
        assertTrue(response.getMessage().contains("Matched with another player"));
        verify(roomCodeDao).createRoomCode(anyString(), eq(3));
        verify(roomCodeDao, times(2)).addPlayerToRoom(anyString(), anyString());
        verify(mockRList).remove("100");
        verify(mockRList).remove("200");
    }

    @Test
    void match_ThreePlayers_QueueSizeThree_Matched() {
        // Given - Third player joins, queue has 3 players, should match last 2
        MatchRequest request = new MatchRequest();
        request.setMode("ranked");
        String playerId = "300";
        List<Object> matchedPlayers = Arrays.asList("200", "300");

        @SuppressWarnings("unchecked")
        RList<Object> mockRList = mock(RList.class);

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(RANKED_QUEUE)).thenReturn(3);
        when(redisService.lRange(RANKED_QUEUE, 1, 2)).thenReturn(matchedPlayers);
        when(redisService.getList(RANKED_QUEUE)).thenReturn(mockRList);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertNotNull(response.getRoomCode());
        assertEquals(2, response.getPlayers().size());
        verify(redisService).lRange(RANKED_QUEUE, 1, 2); // Last 2 players (indices 1 and 2)
        verify(roomCodeDao).createRoomCode(anyString(), eq(3));
    }

    @Test
    void findPlayerQueue_PlayerInCasualQueue() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertEquals("casual", result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService, never()).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void findPlayerQueue_PlayerInRankedQueue() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertEquals("ranked", result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void findPlayerQueue_PlayerNotInAnyQueue() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertNull(result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void findPlayerQueue_CasualQueueNull() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertEquals("ranked", result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void findPlayerQueue_RankedQueueNull() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(null);

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertNull(result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void findPlayerQueue_BothQueuesNull() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(null);

        // When
        String result = matchService.findPlayerQueue(playerId);

        // Then
        assertNull(result);
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
    }

    @Test
    void cancelMatch_PlayerInCasualQueue_Success() {
        // Given
        String playerId = "100";

        @SuppressWarnings("unchecked")
        RList<Object> mockRList = mock(RList.class);

        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));
        when(redisService.getList(CASUAL_QUEUE)).thenReturn(mockRList);
        when(redisService.lSize(CASUAL_QUEUE)).thenReturn(0);

        // When
        CancelMatchResponse response = matchService.cancelMatch(playerId);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("casual", response.getQueueMode());
        assertTrue(response.getMessage().contains("Successfully canceled"));
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(mockRList).remove(playerId);
    }

    @Test
    void cancelMatch_PlayerInRankedQueue_Success() {
        // Given
        String playerId = "100";

        @SuppressWarnings("unchecked")
        RList<Object> mockRList = mock(RList.class);

        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.singletonList("100"));
        when(redisService.getList(RANKED_QUEUE)).thenReturn(mockRList);
        when(redisService.lSize(RANKED_QUEUE)).thenReturn(0);

        // When
        CancelMatchResponse response = matchService.cancelMatch(playerId);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("ranked", response.getQueueMode());
        assertTrue(response.getMessage().contains("Successfully canceled"));
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
        verify(mockRList).remove(playerId);
    }

    @Test
    void cancelMatch_PlayerNotInQueue_NotInQueueStatus() {
        // Given
        String playerId = "100";
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());

        // When
        CancelMatchResponse response = matchService.cancelMatch(playerId);

        // Then
        assertNotNull(response);
        assertEquals("not_in_queue", response.getStatus());
        assertNull(response.getQueueMode());
        assertTrue(response.getMessage().contains("not currently in any match queue"));
        verify(redisService).lRange(CASUAL_QUEUE, 0, -1);
        verify(redisService).lRange(RANKED_QUEUE, 0, -1);
        verify(redisService, never()).getList(anyString());
    }

    @Test
    void match_CasualMode_CaseInsensitive_Uppercase() {
        // Given
        MatchRequest request = new MatchRequest();
        request.setMode("CASUAL");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(CASUAL_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(CASUAL_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        verify(redisService).lPush(CASUAL_QUEUE, playerId);
    }

    @Test
    void match_RankedMode_CaseInsensitive_MixedCase() {
        // Given
        MatchRequest request = new MatchRequest();
        request.setMode("RaNkEd");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(RANKED_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        verify(redisService).lPush(RANKED_QUEUE, playerId);
    }

    @Test
    void match_UnknownMode_DefaultsToRanked() {
        // Given
        MatchRequest request = new MatchRequest();
        request.setMode("unknown");
        String playerId = "100";

        when(roomCodeDao.findRoomCodeByPlayerId(playerId)).thenReturn(null);
        when(redisService.lRange(RANKED_QUEUE, 0, -1)).thenReturn(Collections.emptyList());
        when(redisService.lSize(RANKED_QUEUE)).thenReturn(1);

        // When
        MatchResponse response = matchService.match(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        verify(redisService).lPush(RANKED_QUEUE, playerId);
    }
}
