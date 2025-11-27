package com.goody.nus.se.gomoku.gomoku.game.chain.execute.draw;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for DrawAgreeExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class DrawAgreeExecuteChainTest {

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private DrawAgreeExecuteChain chain;

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
                .drawProposerColor(PlayerColor.BLACK)
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.DRAW_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteDrawAgree() {
        GameAction action = GameAction.builder()
                .type(ActionType.DRAW_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify game is finished
        assertEquals(GameStatus.FINISHED, game.getStatus());

        // Verify winner is 0 (draw)
        assertEquals(0, game.getCurrentState().getWinner());

        // Verify draw proposal is cleared
        assertNull(game.getDrawProposerColor());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.DRAW_AGREE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
