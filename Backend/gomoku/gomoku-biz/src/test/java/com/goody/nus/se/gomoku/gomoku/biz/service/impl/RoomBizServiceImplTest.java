package com.goody.nus.se.gomoku.gomoku.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CreateRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.service.IGameService;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.room.Service.RoomCodeService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.UNKNOWN_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test for RoomBizServiceImpl
 *
 * @author Claude
 * @version 1.0
 */
class RoomBizServiceImplTest {

    @Mock
    private RoomCodeService roomCodeService;

    @Mock
    private IGameService gameService;

    @Mock
    private IGameRoomService gameRoomService;

    @Mock
    private IMatchService matchService;

    @InjectMocks
    private RoomBizServiceImpl roomBizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRoom_Success() {
        String playerId = "123";
        String roomCode = "ABCD1234";

        when(matchService.findPlayerQueue(playerId)).thenReturn(null);
        when(roomCodeService.createRoom()).thenReturn(roomCode);

        CreateRoomResponse response = roomBizService.createRoom(playerId);

        assertNotNull(response);
        assertEquals(roomCode, response.getRoomCode());
        verify(matchService).findPlayerQueue(playerId);
        verify(roomCodeService).createRoom();
    }

    @Test
    void testCreateRoom_PlayerInQueue_ThrowsException() {
        String playerId = "123";
        String queueMode = "casual";

        when(matchService.findPlayerQueue(playerId)).thenReturn(queueMode);

        assertThrows(BizException.class, () -> roomBizService.createRoom(playerId));
        verify(matchService).findPlayerQueue(playerId);
        verify(roomCodeService, never()).createRoom();
    }

    @Test
    void testJoinRoom_Success() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);

        JoinRoomResponse mockResponse = new JoinRoomResponse();
        mockResponse.setStatus("success");

        when(matchService.findPlayerQueue(playerId)).thenReturn(null);
        when(roomCodeService.joinRoom(request, playerId)).thenReturn(mockResponse);

        JoinRoomResponse response = roomBizService.joinRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(matchService).findPlayerQueue(playerId);
        verify(roomCodeService).joinRoom(request, playerId);
    }

    @Test
    void testJoinRoom_PlayerInQueue_ThrowsException() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        String queueMode = "ranked";
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);

        when(matchService.findPlayerQueue(playerId)).thenReturn(queueMode);

        assertThrows(BizException.class, () -> roomBizService.joinRoom(request, playerId));
        verify(matchService).findPlayerQueue(playerId);
        verify(roomCodeService, never()).joinRoom(any(), anyString());
    }

    @Test
    void testLeaveRoom_RoomNotFound() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenThrow(new RuntimeException("Room not found"));
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameService, never()).executeAction(any(), any(), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_RoomFinished() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.FINISHED.getValue());

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);
        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService).findById(roomId);
        verify(gameService, never()).executeAction(any(), any(), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_ActiveRoom_SendsSurrender() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);
        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService).findById(roomId);
        verify(gameService).executeAction(eq(roomId), eq(123L), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_SurrenderFails_StillLeavesRoom() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.PLAYING.getValue());

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);
        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(gameService.executeAction(any(), any(), any())).thenThrow(new BizException(UNKNOWN_ERROR));
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService).findById(roomId);
        verify(gameService).executeAction(eq(roomId), eq(123L), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_RoomIdNull() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(null);
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService, never()).findById(any());
        verify(gameService, never()).executeAction(any(), any(), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_RoomDTONull() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);
        when(gameRoomService.findById(roomId)).thenReturn(null);
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService).findById(roomId);
        // When roomDTO is null, isFinished remains false, so SURRENDER is still sent
        verify(gameService).executeAction(eq(roomId), eq(123L), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }

    @Test
    void testLeaveRoom_FindByIdThrowsException() {
        String playerId = "123";
        String roomCode = "ABCD1234";
        Long roomId = 1L;
        LeaveRoomRequest request = new LeaveRoomRequest();
        request.setRoomCode(roomCode);

        LeaveRoomResponse mockResponse = new LeaveRoomResponse();
        mockResponse.setStatus("success");

        when(gameRoomService.findRoomIdByRoomCode(roomCode)).thenReturn(roomId);
        when(gameRoomService.findById(roomId)).thenThrow(new RuntimeException("Database error"));
        when(roomCodeService.leaveRoom(request, playerId)).thenReturn(mockResponse);

        LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(gameRoomService).findRoomIdByRoomCode(roomCode);
        verify(gameRoomService).findById(roomId);
        // When findById throws exception, isFinished remains false, so SURRENDER is still sent
        verify(gameService).executeAction(eq(roomId), eq(123L), any());
        verify(roomCodeService).leaveRoom(request, playerId);
    }
}
