package com.goody.nus.se.gomoku.gomoku.game.chain.validate.surrender;

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
 * Test for SurrenderPlayingValidateChain
 *
 * @author Claude
 * @version 1.0
 */
class SurrenderPlayingValidateChainTest {

    private SurrenderPlayingValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new SurrenderPlayingValidateChain();
        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.PLAYING)
                .currentState(GameStateSnapshot.createEmpty(15))
                .actionHistory(new ArrayList<>())
                .build();
    }

    @Test
    void testValidateStatus() {
        assertEquals(GameStatus.PLAYING, chain.validateStatus());
    }

    @Test
    void testValidate_AlwaysReturnsTrue() {
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    void testValidate_WhitePlayer() {
        GameAction action = GameAction.builder()
                .type(ActionType.SURRENDER)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertTrue(chain.validate(game, action));
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
