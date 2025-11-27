package com.goody.nus.se.gomoku.gomoku.room.Impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;
import com.goody.nus.se.gomoku.gomoku.enums.RoomTypeEnum;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link RoomCodeServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class RoomCodeServiceImplTest {

    @Mock
    private RoomCodeDao roomCodeDao;

    @Mock
    private IGameRoomService gameRoomService;

    @Mock
    private IRoomStateService roomStateService;

    @InjectMocks
    private RoomCodeServiceImpl roomCodeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRoom_Success_FirstTry() {
        // Given
        when(roomCodeDao.exists(anyString())).thenReturn(false);

        // When
        String roomCode = roomCodeService.createRoom();

        // Then
        assertNotNull(roomCode);
        assertEquals(6, roomCode.length());
        verify(roomCodeDao, times(1)).exists(anyString());
        verify(roomCodeDao, times(1)).createRoomCode(eq(roomCode), eq(3));
    }

    @Test
    void createRoom_Success_AfterRetries() {
        // Given - first 3 attempts fail, 4th succeeds
        when(roomCodeDao.exists(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // When
        String roomCode = roomCodeService.createRoom();

        // Then
        assertNotNull(roomCode);
        assertEquals(6, roomCode.length());
        verify(roomCodeDao, times(4)).exists(anyString());
        verify(roomCodeDao, times(1)).createRoomCode(anyString(), eq(3));
    }

    @Test
    void createRoom_Success_OnSecondTry() {
        // Given - first attempt fails, second succeeds
        when(roomCodeDao.exists(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        // When
        String roomCode = roomCodeService.createRoom();

        // Then
        assertNotNull(roomCode);
        assertEquals(6, roomCode.length());
        verify(roomCodeDao, times(2)).exists(anyString());
        verify(roomCodeDao, times(1)).createRoomCode(anyString(), eq(3));
    }

    @Test
    void createRoom_Success_OnNinthTry() {
        // Given - first 8 attempts fail, 9th succeeds (testing loop continues)
        when(roomCodeDao.exists(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // When
        String roomCode = roomCodeService.createRoom();

        // Then
        assertNotNull(roomCode);
        assertEquals(6, roomCode.length());
        verify(roomCodeDao, times(9)).exists(anyString());
        verify(roomCodeDao, times(1)).createRoomCode(anyString(), eq(3));
    }

    @Test
    void createRoom_Success_OnTenthTry() {
        // Given - first 9 attempts fail, 10th (last) succeeds
        when(roomCodeDao.exists(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // When
        String roomCode = roomCodeService.createRoom();

        // Then
        assertNotNull(roomCode);
        assertEquals(6, roomCode.length());
        verify(roomCodeDao, times(10)).exists(anyString());
        verify(roomCodeDao, times(1)).createRoomCode(anyString(), eq(3));
    }

    @Test
    void createRoom_Failure_MaxRetriesExceeded() {
        // Given - all 10 attempts fail
        when(roomCodeDao.exists(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BizException.class, () -> roomCodeService.createRoom());
        verify(roomCodeDao, times(10)).exists(anyString());
        verify(roomCodeDao, never()).createRoomCode(anyString(), anyInt());
    }

    @Test
    void joinRoom_RoomNotFound() {
        // Given
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(false);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("Not Found", response.getStatus());
        assertNull(response.getRoomId());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao, never()).getPlayersByRoom(anyString());
    }

    @Test
    void joinRoom_PlayerAlreadyIn_OnePlayer_Waiting() {
        // Given - Player is already in room alone (idempotency check)
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(Collections.singletonList("100"));

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertNull(response.getRoomId());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao, never()).addPlayerToRoom(anyString(), anyString());
    }

    @Test
    void joinRoom_PlayerAlreadyIn_TwoPlayers_Matched() {
        // Given - Player is already in room with 2 players (idempotency check)
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";
        List<String> players = Arrays.asList("100", "200");
        Long roomId = 1L;

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(players);
        when(gameRoomService.findRoomIdByRoomCode("123456")).thenReturn(roomId);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        assertEquals(players, response.getPlayers());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(gameRoomService).findRoomIdByRoomCode("123456");
        verify(roomCodeDao, never()).addPlayerToRoom(anyString(), anyString());
    }

    @Test
    void joinRoom_EmptyRoom_FirstPlayer() {
        // Given - Room exists but is empty
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(Collections.emptyList());

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertNull(response.getRoomId());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao).addPlayerToRoom("123456", "100");
        verify(roomCodeDao).updateRoomTTL("123456", 3);
    }

    @Test
    void joinRoom_NullPlayersList_FirstPlayer() {
        // Given - Room exists but players list is null
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(null);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertNull(response.getRoomId());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao).addPlayerToRoom("123456", "100");
        verify(roomCodeDao).updateRoomTTL("123456", 3);
    }

    @Test
    void joinRoom_OnePlayer_SecondPlayerJoins_Matched() {
        // Given - Room has 1 player, second player joins
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "200";
        Long roomId = 1L;

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456"))
                .thenReturn(Collections.singletonList("100")) // First call: 1 player
                .thenReturn(Arrays.asList("100", "200")); // Second call: 2 players
        when(gameRoomService.save(any())).thenReturn(roomId);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertEquals(roomId, response.getRoomId());
        assertEquals(2, response.getPlayers().size());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao, times(2)).getPlayersByRoom("123456");
        verify(roomCodeDao).addPlayerToRoom("123456", "200");
        verify(roomCodeDao).updateRoomTTL("123456", 20);
        verify(gameRoomService).save(any());
        verify(roomStateService).initializeGameState(eq(roomId), eq(100L), eq(200L), eq("PRIVATE"));
    }

    @Test
    void joinRoom_OnePlayer_SecondPlayerJoins_OnlyOnePlayerAfterUpdate() {
        // Given - Edge case: second player joins but after update still only 1 player
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "200";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456"))
                .thenReturn(Collections.singletonList("100")) // First call: 1 player
                .thenReturn(Collections.singletonList("100")); // Second call: still 1 player (edge case)

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("matched", response.getStatus());
        assertNull(response.getRoomId()); // No room record saved
        assertEquals(1, response.getPlayers().size());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao, times(2)).getPlayersByRoom("123456");
        verify(roomCodeDao).addPlayerToRoom("123456", "200");
        verify(roomCodeDao).updateRoomTTL("123456", 20);
        verify(gameRoomService, never()).save(any());
        verify(roomStateService, never()).initializeGameState(any(), any(), any(), any());
    }

    @Test
    void joinRoom_RoomFull_TwoPlayersAlready() {
        // Given - Room has 2 players, third player tries to join
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "300";
        List<String> players = Arrays.asList("100", "200");

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(players);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("full", response.getStatus());
        assertEquals(players, response.getPlayers());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao, never()).addPlayerToRoom(anyString(), anyString());
    }

    @Test
    void joinRoom_RoomFull_MoreThanTwoPlayers() {
        // Given - Room has more than 2 players (edge case)
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "400";
        List<String> players = Arrays.asList("100", "200", "300");

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(players);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("full", response.getStatus());
        assertEquals(players, response.getPlayers());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao, never()).addPlayerToRoom(anyString(), anyString());
    }

    @Test
    void joinRoom_PlayerAlreadyIn_MoreThanTwoPlayers() {
        // Given - Player is already in room with more than 2 players (edge case)
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";
        List<String> players = Arrays.asList("100", "200", "300");

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(players);

        // When
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("full", response.getStatus());
        assertEquals(players, response.getPlayers());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao, never()).addPlayerToRoom(anyString(), anyString());
        verify(gameRoomService, never()).findRoomIdByRoomCode(anyString());
    }

    @Test
    void leaveRoom_RoomNotFound() {
        // Given
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(false);

        // When
        LeaveRoomResponse response = roomCodeService.leaveRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("notFound", response.getStatus());
        assertEquals("Room not found", response.getMessage());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao, never()).removePlayerFromRoom(anyString(), anyString());
    }

    @Test
    void leaveRoom_LastPlayerLeaves_RoomDeleted() {
        // Given - Last player leaves, room should be deleted
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(Collections.emptyList());

        // When
        LeaveRoomResponse response = roomCodeService.leaveRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("empty", response.getStatus());
        assertEquals("Room deleted (no players left)", response.getMessage());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).removePlayerFromRoom("123456", "100");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao).deleteRoom("123456");
        verify(roomCodeDao, never()).updateRoomTTL(anyString(), anyInt());
    }

    @Test
    void leaveRoom_OnePlayerRemains_TTLShortened() {
        // Given - One player leaves, one remains
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(Collections.singletonList("200"));

        // When
        LeaveRoomResponse response = roomCodeService.leaveRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Player left; room TTL shortened to 3 minutes", response.getMessage());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).removePlayerFromRoom("123456", "100");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao).updateRoomTTL("123456", 3);
        verify(roomCodeDao, never()).deleteRoom(anyString());
    }

    @Test
    void leaveRoom_MultiplePlayersRemain_TTLShortened() {
        // Given - One player leaves, multiple remain (edge case)
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode("123456");
        String playerId = "100";

        when(roomCodeDao.exists("123456")).thenReturn(true);
        when(roomCodeDao.getPlayersByRoom("123456")).thenReturn(Arrays.asList("200", "300"));

        // When
        LeaveRoomResponse response = roomCodeService.leaveRoom(request, playerId);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Player left; room TTL shortened to 3 minutes", response.getMessage());
        verify(roomCodeDao).exists("123456");
        verify(roomCodeDao).removePlayerFromRoom("123456", "100");
        verify(roomCodeDao).getPlayersByRoom("123456");
        verify(roomCodeDao).updateRoomTTL("123456", 3);
        verify(roomCodeDao, never()).deleteRoom(anyString());
    }

    @Test
    void saveRoomRecord_Success() {
        // Given
        String roomCode = "123456";
        Long player1Id = 100L;
        Long player2Id = 200L;
        byte roomType = RoomTypeEnum.PRIVATE.getValue();
        Long expectedRoomId = 1L;

        when(gameRoomService.save(any())).thenReturn(expectedRoomId);

        // When
        Long roomId = roomCodeService.saveRoomRecord(roomCode, player1Id, player2Id, roomType);

        // Then
        assertNotNull(roomId);
        assertEquals(expectedRoomId, roomId);
        verify(gameRoomService).save(argThat(dto ->
                dto.getRoomCode().equals(roomCode) &&
                dto.getPlayer1Id().equals(player1Id) &&
                dto.getPlayer2Id().equals(player2Id) &&
                dto.getType() == roomType
        ));
    }

    @Test
    void saveRoomRecord_DifferentRoomTypes() {
        // Given
        String roomCode = "123456";
        Long player1Id = 100L;
        Long player2Id = 200L;
        Long expectedRoomId = 1L;

        when(gameRoomService.save(any())).thenReturn(expectedRoomId);

        // When - Test with different room types
        byte casualType = RoomTypeEnum.CASUAL.getValue();
        Long casualRoomId = roomCodeService.saveRoomRecord(roomCode, player1Id, player2Id, casualType);

        byte rankedType = RoomTypeEnum.RANKED.getValue();
        Long rankedRoomId = roomCodeService.saveRoomRecord(roomCode, player1Id, player2Id, rankedType);

        // Then
        assertEquals(expectedRoomId, casualRoomId);
        assertEquals(expectedRoomId, rankedRoomId);
        verify(gameRoomService, times(2)).save(any());
    }
}
