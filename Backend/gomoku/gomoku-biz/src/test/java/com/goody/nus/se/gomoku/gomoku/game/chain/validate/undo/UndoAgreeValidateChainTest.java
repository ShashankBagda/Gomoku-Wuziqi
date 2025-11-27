package com.goody.nus.se.gomoku.gomoku.game.chain.validate.undo;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for UndoAgreeValidateChain
 *
 * @author Claude
 * @version 1.0
 */
class UndoAgreeValidateChainTest {

    private UndoAgreeValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new UndoAgreeValidateChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .undoProposerColor(PlayerColor.BLACK)
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidate_Success() {
        // BLACK proposed undo, WHITE agrees
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidate_Success_WhiteProposedBlackAgrees() {
        game.setUndoProposerColor(PlayerColor.WHITE);

        // WHITE proposed undo, BLACK agrees
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenNoPendingProposal() {
        game.setUndoProposerColor(null); // No pending proposal

        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenProposerTriesToAgree() {
        // BLACK proposed undo, BLACK tries to agree (invalid)
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testValidate_FailsWhenWhiteProposerTriesToAgree() {
        game.setUndoProposerColor(PlayerColor.WHITE);

        // WHITE proposed undo, WHITE tries to agree (invalid)
        GameAction action = GameAction.builder()
                .type(ActionType.UNDO_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.UNDO_AGREE));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
