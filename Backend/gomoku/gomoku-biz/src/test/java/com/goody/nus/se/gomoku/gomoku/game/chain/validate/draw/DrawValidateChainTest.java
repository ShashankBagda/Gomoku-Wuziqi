package com.goody.nus.se.gomoku.gomoku.game.chain.validate.draw;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for DrawValidateChain
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
class DrawValidateChainTest {

    private DrawValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new DrawValidateChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .drawProposerColor(null)
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidateWithNoPendingDrawProposal() {
        GameAction action = GameAction.builder()
                .type(ActionType.DRAW)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidateWithExistingDrawProposal() {
        game.setDrawProposerColor(PlayerColor.BLACK);

        GameAction action = GameAction.builder()
                .type(ActionType.DRAW)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.DRAW));
    }

    @Test
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
