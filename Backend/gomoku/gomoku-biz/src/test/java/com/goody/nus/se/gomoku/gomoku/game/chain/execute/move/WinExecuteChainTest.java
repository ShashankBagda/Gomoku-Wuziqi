package com.goody.nus.se.gomoku.gomoku.game.chain.execute.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for WinExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class WinExecuteChainTest {

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private WinExecuteChain chain;

    private GameDocument game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setCurrentTurn(PlayerColor.BLACK);
        state.setTotalMoves(0);

        // Setup a board with 4 black stones in a row
        state.getBoard()[7][5] = PlayerColor.BLACK.getValue();
        state.getBoard()[7][6] = PlayerColor.BLACK.getValue();
        state.getBoard()[7][7] = PlayerColor.BLACK.getValue();
        state.getBoard()[7][8] = PlayerColor.BLACK.getValue();

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(state)
                .build();
    }

    @Test
    void testCheckWithWinningMove() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(9).build())
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWithNonWinningMove() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(10).y(10).build())
                .build();

        assertFalse(chain.check(game, action));
    }

    @Test
    void testExecuteWinningMove() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(9).build())
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify stone is placed
        assertEquals(PlayerColor.BLACK.getValue(), game.getCurrentState().getBoard()[7][9]);

        // Verify game is finished
        assertEquals(GameStatus.FINISHED, game.getStatus());

        // Verify winner is black
        assertEquals(PlayerColor.BLACK.getValue(), game.getCurrentState().getWinner());

        // Verify total moves incremented
        assertEquals(1, game.getCurrentState().getTotalMoves());
    }

    @Test
    void testExecuteClearsDrawProposal() {
        game.setDrawProposerColor(PlayerColor.WHITE);

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(9).build())
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        assertNull(game.getDrawProposerColor());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.MOVE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
