package com.goody.nus.se.gomoku.gomoku.game.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.GameStateResponse;
import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChainHandler;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChainHandler;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameRepository;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for GameServiceImpl edge cases
 *
 * @author Claude
 * @version 1.0
 */
class GameServiceImplUnitTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ValidateChainHandler validateChainHandler;

    @Mock
    private ExecuteChainHandler executeChainHandler;

    @Mock
    private IGameRoomService gameRoomService;

    @Mock
    private RoomCodeDao roomCodeDao;

    @InjectMocks
    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteAction_RoomNotFound() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);

        when(gameRoomService.findById(roomId)).thenReturn(null);

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRoomService).findById(roomId);
    }

    @Test
    void testExecuteAction_RoomFinished_NonRestartAction() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.FINISHED.getValue());
        roomDTO.setRoomCode("ABCD1234");

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRoomService).findById(roomId);
    }

    @Test
    void testExecuteAction_RoomFinished_RestartAction() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.RESTART);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.FINISHED.getValue());
        roomDTO.setRoomCode("ABCD1234");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRoomService).findById(roomId);
        verify(roomCodeDao).updateRoomTTL("ABCD1234", 20);
        verify(validateChainHandler).handle(any(), any());
        verify(executeChainHandler).handle(any(), any());
        verify(gameRepository).save(any());
    }

    @Test
    void testExecuteAction_RestartAgreeAction_AllowedWhenFinished() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.RESTART_AGREE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.FINISHED.getValue());
        roomDTO.setRoomCode("ABCD1234");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRoomService).findById(roomId);
        verify(validateChainHandler).handle(any(), any());
    }

    @Test
    void testExecuteAction_RestartDisagreeAction_AllowedWhenFinished() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.RESTART_DISAGREE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.FINISHED.getValue());
        roomDTO.setRoomCode("ABCD1234");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRoomService).findById(roomId);
        verify(validateChainHandler).handle(any(), any());
    }

    @Test
    void testExecuteAction_RoomCodeNotExist() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("ABCD1234");

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(false);

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRoomService).findById(roomId);
        verify(roomCodeDao).exists("ABCD1234");
    }

    @Test
    void testExecuteAction_SurrenderAction_SkipsValidation() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.SURRENDER);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.PLAYING.getValue());
        roomDTO.setRoomCode("ABCD1234");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        gameService.executeAction(roomId, playerId, request);

        verify(validateChainHandler, never()).handle(any(), any());
        verify(executeChainHandler).handle(any(), any());
        verify(gameRepository).save(any());
    }

    @Test
    void testExecuteAction_ValidationFails() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);
        request.setPosition(new Position(7, 7));

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.PLAYING.getValue());
        roomDTO.setRoomCode("ABCD1234");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(false);

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(validateChainHandler).handle(any(), any());
        verify(executeChainHandler, never()).handle(any(), any());
    }

    @Test
    void testExecuteAction_CreateNewGame_WhenNotFound() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.READY);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("ABCD1234");

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("ABCD1234")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRepository).findByRoomId(roomId);
        verify(validateChainHandler).handle(any(), any());
        verify(executeChainHandler).handle(any(), any());
        verify(gameRepository).save(any());
    }

    @Test
    void testGetState_GameNotFound() {
        Long roomId = 1L;
        Long playerId = 100L;

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> gameService.getState(roomId, playerId));
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testGetState_PlayerNotInGame_WhilePlaying() {
        Long roomId = 1L;
        Long playerId = 300L; // Not in game

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        assertThrows(BizException.class, () -> gameService.getState(roomId, playerId));
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testGetState_Success_WhileWaiting() {
        Long roomId = 1L;
        Long playerId = 300L; // Not in game yet, but status is WAITING

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        GameStateResponse response = gameService.getState(roomId, playerId);

        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testGetState_Success_BlackPlayer() {
        Long roomId = 1L;
        Long playerId = 100L; // Black player

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        GameStateResponse response = gameService.getState(roomId, playerId);

        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertEquals(100L, response.getBlackPlayerId());
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testGetState_Success_WhitePlayer() {
        Long roomId = 1L;
        Long playerId = 200L; // White player

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        GameStateResponse response = gameService.getState(roomId, playerId);

        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertEquals(200L, response.getWhitePlayerId());
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testExecuteAction_RoomCodeNull() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode(null);  // Null room code

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRoomService).findById(roomId);
    }

    @Test
    void testExecuteAction_UpdatesRoomCodeTTL() {
        Long roomId = 1L;
        Long playerId = 100L;
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.READY);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("TESTROOM");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(null)
                .whitePlayerId(null)
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("TESTROOM")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(roomCodeDao).updateRoomTTL("TESTROOM", 20);
        verify(gameRepository).save(any());
    }

    @Test
    void testExecuteAction_PlayerNotInGame_WhilePlaying() {
        Long roomId = 1L;
        Long playerId = 300L; // Not in game
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.MOVE);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.PLAYING.getValue());
        roomDTO.setRoomCode("TESTROOM");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("TESTROOM")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRepository).findByRoomId(roomId);
    }

    @Test
    void testExecuteAction_WhitePlayerJoinsWaiting_BlackPlayerExists() {
        Long roomId = 1L;
        Long playerId = 200L; // White player joining
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.READY);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("TESTROOM");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)
                .whitePlayerId(null)  // White slot empty
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("TESTROOM")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRepository).save(any());
        verify(executeChainHandler).handle(any(), any());
    }

    @Test
    void testExecuteAction_BlackPlayerJoinsWaiting_WhitePlayerExists() {
        Long roomId = 1L;
        Long playerId = 100L; // Black player joining
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.READY);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("TESTROOM");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(null)  // Black slot empty
                .whitePlayerId(200L)
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("TESTROOM")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));
        when(validateChainHandler.handle(any(), any())).thenReturn(true);

        gameService.executeAction(roomId, playerId, request);

        verify(gameRepository).save(any());
        verify(executeChainHandler).handle(any(), any());
    }

    @Test
    void testExecuteAction_PlayerNotInGame_WaitingButBothSlotsOccupied() {
        Long roomId = 1L;
        Long playerId = 300L; // Third player trying to join
        GomokuActionRequest request = new GomokuActionRequest();
        request.setType(ActionType.READY);

        GameRoomDTO roomDTO = new GameRoomDTO();
        roomDTO.setId(roomId);
        roomDTO.setStatus(RoomStatusEnum.WAITING.getValue());
        roomDTO.setRoomCode("TESTROOM");

        GameDocument game = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(100L)  // Black slot occupied
                .whitePlayerId(200L)  // White slot occupied
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();

        when(gameRoomService.findById(roomId)).thenReturn(roomDTO);
        when(roomCodeDao.exists("TESTROOM")).thenReturn(true);
        when(gameRepository.findByRoomId(roomId)).thenReturn(Optional.of(game));

        assertThrows(BizException.class, () -> gameService.executeAction(roomId, playerId, request));
        verify(gameRepository).findByRoomId(roomId);
    }
}
