package com.goody.nus.se.gomoku.gomoku.game.chain.execute.move;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for NormalMoveExecuteChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class NormalMoveExecuteChainTest {

    private NormalMoveExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new NormalMoveExecuteChain();

        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setCurrentTurn(PlayerColor.BLACK);
        state.setTotalMoves(0);

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(state)
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteNormalMove() {
        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify stone is placed
        assertEquals(PlayerColor.BLACK.getValue(), game.getCurrentState().getBoard()[7][7]);

        // Verify turn switched
        assertEquals(PlayerColor.WHITE, game.getCurrentState().getCurrentTurn());

        // Verify total moves incremented
        assertEquals(1, game.getCurrentState().getTotalMoves());

        // Verify game is still playing
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void testExecuteClearsDrawProposal() {
        game.setDrawProposerColor(PlayerColor.WHITE);

        GameAction action = GameAction.builder()
                .type(ActionType.MOVE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .position(Position.builder().x(7).y(7).build())
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
        assertEquals(3, chain.sort());
    }
}
