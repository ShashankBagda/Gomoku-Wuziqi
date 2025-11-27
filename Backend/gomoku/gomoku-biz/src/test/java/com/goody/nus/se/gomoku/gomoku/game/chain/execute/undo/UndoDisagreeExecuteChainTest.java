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
 * Test for UndoDisagreeExecuteChain
 *
 * @author Claude
 * @version 1.0
 */
class UndoDisagreeExecuteChainTest {

    private UndoDisagreeExecuteChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new UndoDisagreeExecuteChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .undoProposerColor(PlayerColor.BLACK)
                .build();
    }

    @Test
    void testCheck() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_DISAGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.check(game, action));
    }

    @Test
    void testExecuteUndoDisagree() {
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_DISAGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify undo proposer is cleared
        assertNull(game.getUndoProposerColor());

        // Verify game is still playing
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void testExecuteUndoDisagree_BlackDisagrees() {
        game.setUndoProposerColor(PlayerColor.WHITE);

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .timestamp(System.currentTimeMillis())
                .build();

        chain.execute(game, action);

        // Verify undo proposer is cleared
        assertNull(game.getUndoProposerColor());

        // Verify game is still playing
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.UNDO_DISAGREE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
