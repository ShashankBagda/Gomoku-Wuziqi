package com.goody.nus.se.gomoku.gomoku.game.chain.execute.surrender;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for SurrenderExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class SurrenderExecuteChainTest {

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private SurrenderExecuteChain chain;

    private GameDocument game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteBlackSurrenders() {
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify game is finished
        assertEquals(GameStatus.FINISHED, game.getStatus());

        // Verify opponent (WHITE) wins
        assertEquals(PlayerColor.WHITE.getValue(), game.getCurrentState().getWinner());
    }

    @Test
    void testExecuteWhiteSurrenders() {
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify game is finished
        assertEquals(GameStatus.FINISHED, game.getStatus());

        // Verify opponent (BLACK) wins
        assertEquals(PlayerColor.BLACK.getValue(), game.getCurrentState().getWinner());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.SURRENDER));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
