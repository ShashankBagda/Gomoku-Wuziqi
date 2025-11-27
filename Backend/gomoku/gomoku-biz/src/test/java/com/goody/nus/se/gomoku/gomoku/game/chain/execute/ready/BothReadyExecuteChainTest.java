package com.goody.nus.se.gomoku.gomoku.game.chain.execute.ready;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for BothReadyExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class BothReadyExecuteChainTest {

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private BothReadyExecuteChain chain;

    private GameDocument game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.WAITING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .blackReady(false)
                .whiteReady(false)
                .build();
    }

    @Test
    void testCheckWhenBlackReadyAndWhiteAlreadyReady() {
        game.setWhiteReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWhenWhiteReadyAndBlackAlreadyReady() {
        game.setBlackReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWhenOpponentNotReady() {
        game.setBlackReady(false);
        game.setWhiteReady(false);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.check(game, action));
    }

    @Test
    void testExecuteBlackReady() {
        game.setWhiteReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify black is marked as ready
        assertTrue(game.getBlackReady());

        // Verify game status changed to PLAYING
        assertEquals(GameStatus.PLAYING, game.getStatus());

        // Verify current turn is set to BLACK
        assertEquals(PlayerColor.BLACK, game.getCurrentState().getCurrentTurn());
    }

    @Test
    void testExecuteWhiteReady() {
        game.setBlackReady(true);

        GameAction action = GameAction.builder()
                .type(ActionType.READY)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify white is marked as ready
        assertTrue(game.getWhiteReady());

        // Verify game status changed to PLAYING
        assertEquals(GameStatus.PLAYING, game.getStatus());

        // Verify current turn is set to BLACK (BLACK always goes first)
        assertEquals(PlayerColor.BLACK, game.getCurrentState().getCurrentTurn());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.READY));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
