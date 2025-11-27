package com.goody.nus.se.gomoku.gomoku.game.chain.execute.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for UndoExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class UndoExecuteChainTest {

    private UndoExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new UndoExecuteChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .undoProposerColor(null)
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteUndoProposal_BlackPlayer() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify undo proposer is set
        assertEquals(PlayerColor.BLACK, game.getUndoProposerColor());

        // Verify game is still playing
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void testExecuteUndoProposal_WhitePlayer() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify undo proposer is set to WHITE
        assertEquals(PlayerColor.WHITE, game.getUndoProposerColor());

        // Verify game is still playing
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.UNDO));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
