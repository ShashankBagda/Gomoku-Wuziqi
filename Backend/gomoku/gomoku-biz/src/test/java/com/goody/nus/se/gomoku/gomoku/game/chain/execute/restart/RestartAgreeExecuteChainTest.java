package com.goody.nus.se.gomoku.gomoku.game.chain.execute.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameHistoryService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for RestartAgreeExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class RestartAgreeExecuteChainTest {

    @Mock
    private IGameHistoryService gameHistoryService;

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private RestartAgreeExecuteChain chain;

    private GameDocument game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a finished game with restart proposal
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1); // Black wins
        state.setTotalMoves(25);

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(state)
                .restartProposerColor(PlayerColor.BLACK)
                .gameCount(1)
                .actionHistory(new ArrayList<>())
                .blackReady(true)
                .whiteReady(true)
                .build();

        // Mock gameHistoryService.archiveGame()
        when(gameHistoryService.archiveGame(any(GameDocument.class), anyInt(), anyString()))
                .thenReturn(GameHistoryDocument.builder().build());
    }

    @Test
    @DisplayName("Should always pass check")
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    @DisplayName("Should archive game and reset state when RESTART_AGREE is executed")
    void testExecuteRestartAgree() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        // Execute
        chain.execute(game, action);

        // Verify archive was called with correct parameters
        ArgumentCaptor<GameDocument> gameCaptor = ArgumentCaptor.forClass(GameDocument.class);
        ArgumentCaptor<Integer> gameNumberCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);

        verify(gameHistoryService).archiveGame(
                gameCaptor.capture(),
                gameNumberCaptor.capture(),
                endReasonCaptor.capture()
        );

        assertEquals(1L, gameCaptor.getValue().getRoomId());
        assertEquals(1, gameNumberCaptor.getValue());
        assertEquals("WIN", endReasonCaptor.getValue());

        // Verify game state was reset
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(2, game.getGameCount()); // Incremented from 1 to 2
        assertFalse(game.getBlackReady());
        assertFalse(game.getWhiteReady());
        assertNull(game.getRestartProposerColor());

        // Verify colors were swapped
        assertEquals(200L, game.getBlackPlayerId()); // Was white, now black
        assertEquals(100L, game.getWhitePlayerId()); // Was black, now white

        // Verify state was reset
        assertEquals(0, game.getCurrentState().getTotalMoves());
        assertEquals(-1, game.getCurrentState().getWinner());
    }

    @Test
    @DisplayName("Should handle draw end reason")
    void testExecuteRestartAgreeWithDraw() {
        game.getCurrentState().setWinner(0); // Draw

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("DRAW", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle surrender end reason")
    void testExecuteRestartAgreeWithSurrender() {
        GameAction surrenderAction = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();
        game.setLastAction(surrenderAction);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("SURRENDER", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should return correct action types")
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.RESTART_AGREE));
    }

    @Test
    @DisplayName("Should return correct sort order")
    void testSort() {
        assertEquals(1, chain.sort());
    }

    @Test
    @DisplayName("Should handle null currentState as UNKNOWN")
    void testExecuteRestartAgreeWithNullState() {
        game.setCurrentState(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("UNKNOWN", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle null winner as ONGOING")
    void testExecuteRestartAgreeWithNullWinner() {
        game.getCurrentState().setWinner(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("ONGOING", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle winner=-1 as ONGOING")
    void testExecuteRestartAgreeWithWinnerNegativeOne() {
        game.getCurrentState().setWinner(-1);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("ONGOING", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle timeout end reason")
    void testExecuteRestartAgreeWithTimeout() {
        GameAction timeoutAction = GameAction.builder()
                .type(ActionType.TIMEOUT)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();
        game.setLastAction(timeoutAction);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("TIMEOUT", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle draw agree end reason")
    void testExecuteRestartAgreeWithDrawAgree() {
        game.getCurrentState().setWinner(0);
        GameAction drawAgreeAction = GameAction.builder()
                .type(ActionType.DRAW_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();
        game.setLastAction(drawAgreeAction);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("DRAW", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle null lastAction with winner")
    void testExecuteRestartAgreeWithNullLastAction() {
        game.getCurrentState().setWinner(1);
        game.setLastAction(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<String> endReasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(gameHistoryService).archiveGame(any(), anyInt(), endReasonCaptor.capture());

        assertEquals("WIN", endReasonCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle null gameCount")
    void testExecuteRestartAgreeWithNullGameCount() {
        game.setGameCount(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        chain.execute(game, action);

        ArgumentCaptor<Integer> gameNumberCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(gameHistoryService).archiveGame(any(), gameNumberCaptor.capture(), anyString());

        assertEquals(1, gameNumberCaptor.getValue());
    }
}
