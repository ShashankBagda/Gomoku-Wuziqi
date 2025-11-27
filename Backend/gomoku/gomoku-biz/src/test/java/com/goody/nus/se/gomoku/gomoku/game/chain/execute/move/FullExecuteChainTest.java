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
 * Test for FullExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class FullExecuteChainTest {

    @Mock
    private IGameRoomService gameRoomService;

    @InjectMocks
    private FullExecuteChain chain;

    private GameDocument game;
    private GameStateSnapshot state;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        state = GameStateSnapshot.createEmpty(3); // Small board for testing
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(state)
                .build();
    }

    @Test
    void testCheckWhenBoardWillBeFull() {
        // Fill all positions except (2, 2)
        int[][] board = state.getBoard();
        board[0][0] = PlayerColor.BLACK.getValue();
        board[0][1] = PlayerColor.WHITE.getValue();
        board[0][2] = PlayerColor.BLACK.getValue();
        board[1][0] = PlayerColor.WHITE.getValue();
        board[1][1] = PlayerColor.BLACK.getValue();
        board[1][2] = PlayerColor.WHITE.getValue();
        board[2][0] = PlayerColor.BLACK.getValue();
        board[2][1] = PlayerColor.WHITE.getValue();
        // board[2][2] is empty

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(2, 2))
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testCheckWhenBoardWillNotBeFull() {
        // Leave multiple positions empty
        int[][] board = state.getBoard();
        board[0][0] = PlayerColor.BLACK.getValue();
        board[0][1] = PlayerColor.WHITE.getValue();
        // board[0][2], board[1][*], board[2][*] are empty

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(0, 2))
                .build();

        assertFalse(chain.check(game, action));
    }

    @Test
    void testExecuteFullBoard() {
        // Fill all positions except (2, 2)
        int[][] board = state.getBoard();
        board[0][0] = PlayerColor.BLACK.getValue();
        board[0][1] = PlayerColor.WHITE.getValue();
        board[0][2] = PlayerColor.BLACK.getValue();
        board[1][0] = PlayerColor.WHITE.getValue();
        board[1][1] = PlayerColor.BLACK.getValue();
        board[1][2] = PlayerColor.WHITE.getValue();
        board[2][0] = PlayerColor.BLACK.getValue();
        board[2][1] = PlayerColor.WHITE.getValue();

        state.setTotalMoves(8);

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(2, 2))
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify stone is placed
        assertEquals(PlayerColor.BLACK.getValue(), board[2][2]);

        // Verify move count is incremented
        assertEquals(9, state.getTotalMoves());

        // Verify game is finished with draw (winner = 0)
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(0, state.getWinner());

        // Verify snapshot time is updated
        assertTrue(state.getSnapshotTime() > 0);
    }

    @Test
    void testExecuteClearsDrawProposal() {
        // Set up a draw proposal
        game.setDrawProposerColor(PlayerColor.WHITE);

        // Fill all positions except (2, 2)
        int[][] board = state.getBoard();
        board[0][0] = PlayerColor.BLACK.getValue();
        board[0][1] = PlayerColor.WHITE.getValue();
        board[0][2] = PlayerColor.BLACK.getValue();
        board[1][0] = PlayerColor.WHITE.getValue();
        board[1][1] = PlayerColor.BLACK.getValue();
        board[1][2] = PlayerColor.WHITE.getValue();
        board[2][0] = PlayerColor.BLACK.getValue();
        board[2][1] = PlayerColor.WHITE.getValue();

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(new Position(2, 2))
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify draw proposal is cleared
        assertNull(game.getDrawProposerColor());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.MOVE));
    }

    @Test
    void testSort() {
        assertEquals(2, chain.sort());
    }
}
