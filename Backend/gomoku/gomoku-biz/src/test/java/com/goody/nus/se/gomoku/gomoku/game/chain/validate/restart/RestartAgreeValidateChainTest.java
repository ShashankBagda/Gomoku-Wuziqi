package com.goody.nus.se.gomoku.gomoku.game.chain.validate.restart;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for RestartAgreeValidateChain
 *
 * @author Claude
 * @version 1.0
 */
class RestartAgreeValidateChainTest {

    private RestartAgreeValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new RestartAgreeValidateChain();

        // Create a finished game with pending restart proposal
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(1); // Black wins

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(state)
                .restartProposerColor(PlayerColor.BLACK) // Black proposed restart
                .build();
    }

    @Test
    @DisplayName("Should require FINISHED status")
    void testValidateStatus() {
        assertEquals(GameStatus.FINISHED, chain.validateStatus());
    }

    @Test
    @DisplayName("Should allow RESTART_AGREE when opponent responds")
    void testValidateOpponentAgrees() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE) // White agrees
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should reject RESTART_AGREE when no pending proposal")
    void testValidateWithNoPendingProposal() {
        game.setRestartProposerColor(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should reject RESTART_AGREE when proposer tries to agree to own proposal")
    void testValidateProposerCannotAgree() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_AGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK) // Proposer tries to agree
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should return correct action types")
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.RESTART_AGREE));
    }

    @Test
    @DisplayName("Should return correct sort order")
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
