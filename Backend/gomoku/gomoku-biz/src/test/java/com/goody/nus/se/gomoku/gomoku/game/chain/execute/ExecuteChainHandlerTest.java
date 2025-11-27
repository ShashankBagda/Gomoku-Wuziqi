package com.goody.nus.se.gomoku.gomoku.game.chain.execute;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for ExecuteChainHandler
 *
 * @author Claude
 * @version 1.0
 */
class ExecuteChainHandlerTest {

    private ExecuteChainHandler handler;
    private ExecuteChain mockChain1;
    private ExecuteChain mockChain2;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        // Create mock chains
        mockChain1 = mock(ExecuteChain.class);
        mockChain2 = mock(ExecuteChain.class);

        // Configure mock chain 1 - handles READY action
        when(mockChain1.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(mockChain1.sort()).thenReturn(1);

        // Configure mock chain 2 - handles MOVE action
        when(mockChain2.getActionTypes()).thenReturn(List.of(ActionType.MOVE));
        when(mockChain2.sort()).thenReturn(2);

        // Create handler with mock chains
        List<ExecuteChain> chains = List.of(mockChain1, mockChain2);
        handler = new ExecuteChainHandler(chains);
        handler.init();

        // Create test game document
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.WAITING)
                .currentState(state)
                .actionHistory(new ArrayList<>())
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Should execute matching chain when check returns true")
    void testHandleExecutesMatchingChain() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain1.check(game, action)).thenReturn(true);

        Long initialVersion = game.getVersion();
        handler.handle(game, action);

        // Verify chain1 was checked and executed
        verify(mockChain1).check(game, action);
        verify(mockChain1).execute(game, action);

        // Verify game state was updated
        assertEquals(initialVersion + 1, game.getVersion());
        assertEquals(action, game.getLastAction());
        assertTrue(game.getActionHistory().contains(action));
        assertNotNull(game.getUpdateTime());

        // Verify chain2 was not involved
        verify(mockChain2, never()).check(any(), any());
        verify(mockChain2, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Should not execute chain when check returns false")
    void testHandleSkipsNonMatchingChain() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain1.check(game, action)).thenReturn(false);

        handler.handle(game, action);

        // Verify chain1 was checked but not executed
        verify(mockChain1).check(game, action);
        verify(mockChain1, never()).execute(any(), any());

        // Verify game state was NOT updated
        assertEquals(0L, game.getVersion());
        assertNull(game.getLastAction());
        assertTrue(game.getActionHistory().isEmpty());
    }

    @Test
    @DisplayName("Should handle action with no matching chains")
    void testHandleNoMatchingChains() {
        // Create action type that no chain handles
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        Long initialVersion = game.getVersion();
        handler.handle(game, action);

        // Verify no chains were executed
        verify(mockChain1, never()).check(any(), any());
        verify(mockChain1, never()).execute(any(), any());
        verify(mockChain2, never()).check(any(), any());
        verify(mockChain2, never()).execute(any(), any());

        // Verify game state was not updated
        assertEquals(initialVersion, game.getVersion());
        assertNull(game.getLastAction());
        assertTrue(game.getActionHistory().isEmpty());
    }

    @Test
    @DisplayName("Should execute correct chain for MOVE action")
    void testHandleMoveAction() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain2.check(game, action)).thenReturn(true);

        handler.handle(game, action);

        // Verify chain2 was checked and executed
        verify(mockChain2).check(game, action);
        verify(mockChain2).execute(game, action);

        // Verify chain1 was not involved
        verify(mockChain1, never()).check(any(), any());
        verify(mockChain1, never()).execute(any(), any());

        // Verify game state was updated
        assertEquals(1L, game.getVersion());
        assertEquals(action, game.getLastAction());
    }

    @Test
    @DisplayName("Should stop at first matching chain")
    void testHandleStopsAtFirstMatch() {
        // Create a chain that handles multiple action types
        ExecuteChain mockChain3 = mock(ExecuteChain.class);
        when(mockChain3.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(mockChain3.sort()).thenReturn(0); // Lower sort order, executes first

        List<ExecuteChain> chains = List.of(mockChain3, mockChain1, mockChain2);
        handler = new ExecuteChainHandler(chains);
        handler.init();

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain3.check(game, action)).thenReturn(true);
        when(mockChain1.check(game, action)).thenReturn(true);

        handler.handle(game, action);

        // Verify only chain3 was executed (it matched first)
        verify(mockChain3).check(game, action);
        verify(mockChain3).execute(game, action);

        // Verify chain1 was not checked (stopped after first match)
        verify(mockChain1, never()).check(any(), any());
        verify(mockChain1, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Should add action to history")
    void testHandleAddsActionToHistory() {
        GameAction action1 = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(1000L)
                .build();

        GameAction action2 = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(2000L)
                .build();

        when(mockChain1.check(game, action1)).thenReturn(true);
        when(mockChain2.check(game, action2)).thenReturn(true);

        handler.handle(game, action1);
        assertEquals(1, game.getActionHistory().size());
        assertEquals(action1, game.getActionHistory().get(0));

        handler.handle(game, action2);
        assertEquals(2, game.getActionHistory().size());
        assertEquals(action2, game.getActionHistory().get(1));
    }

    @Test
    @DisplayName("Should increment version on each execution")
    void testHandleIncrementsVersion() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain1.check(game, action)).thenReturn(true);

        assertEquals(0L, game.getVersion());

        handler.handle(game, action);
        assertEquals(1L, game.getVersion());

        handler.handle(game, action);
        assertEquals(2L, game.getVersion());

        handler.handle(game, action);
        assertEquals(3L, game.getVersion());
    }

    @Test
    @DisplayName("Should update game timestamp")
    void testHandleUpdatesTimestamp() {
        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        when(mockChain1.check(game, action)).thenReturn(true);

        game.setUpdateTime(null);
        assertNull(game.getUpdateTime());

        long beforeHandle = System.currentTimeMillis();
        handler.handle(game, action);
        long afterHandle = System.currentTimeMillis();

        assertNotNull(game.getUpdateTime());
        assertTrue(game.getUpdateTime() >= beforeHandle);
        assertTrue(game.getUpdateTime() <= afterHandle);
    }

    @Test
    @DisplayName("Should handle multiple chains for same action type")
    void testHandleMultipleChainsForSameActionType() {
        ExecuteChain mockChain3 = mock(ExecuteChain.class);
        when(mockChain3.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(mockChain3.sort()).thenReturn(1);

        List<ExecuteChain> chains = List.of(mockChain1, mockChain3, mockChain2);
        handler = new ExecuteChainHandler(chains);
        handler.init();

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        // First chain returns false, second chain returns true
        when(mockChain1.check(game, action)).thenReturn(false);
        when(mockChain3.check(game, action)).thenReturn(true);

        handler.handle(game, action);

        // Verify both chains were checked
        verify(mockChain1).check(game, action);
        verify(mockChain3).check(game, action);

        // Verify only chain3 was executed
        verify(mockChain1, never()).execute(any(), any());
        verify(mockChain3).execute(game, action);
    }

    @Test
    @DisplayName("Should initialize chains in sorted order")
    void testInitSortsChainsCorrectly() {
        ExecuteChain chain1 = mock(ExecuteChain.class);
        ExecuteChain chain2 = mock(ExecuteChain.class);
        ExecuteChain chain3 = mock(ExecuteChain.class);

        when(chain1.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(chain1.sort()).thenReturn(3);

        when(chain2.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(chain2.sort()).thenReturn(1);

        when(chain3.getActionTypes()).thenReturn(List.of(ActionType.READY));
        when(chain3.sort()).thenReturn(2);

        // Chains provided in unsorted order
        List<ExecuteChain> chains = List.of(chain1, chain2, chain3);
        ExecuteChainHandler handler = new ExecuteChainHandler(chains);
        handler.init();

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        // chain2 returns true (sort order 1, should be checked first)
        when(chain2.check(any(), any())).thenReturn(true);

        handler.handle(game, action);

        // Verify chain2 was executed (lowest sort order)
        verify(chain2).check(any(), any());
        verify(chain2).execute(any(), any());

        // Verify other chains were not checked (stopped after first match)
        verify(chain3, never()).check(any(), any());
        verify(chain1, never()).check(any(), any());
    }
}
