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
 * Unit test for RestartDisagreeValidateChain
 *
 * @author Claude
 * @version 1.0
 */
class RestartDisagreeValidateChainTest {

    private RestartDisagreeValidateChain chain;
    private GameDocument game;

    @BeforeEach
    void setUp() {
        chain = new RestartDisagreeValidateChain();

        // Create a finished game with pending restart proposal
        GameStateSnapshot state = GameStateSnapshot.createEmpty(15);
        state.setWinner(2); // White wins

        game = GameDocument.builder()
                .roomId(1L)
                .blackPlayerId(100L)
                .whitePlayerId(200L)
                .status(GameStatus.FINISHED)
                .currentState(state)
                .restartProposerColor(PlayerColor.WHITE) // White proposed restart
                .build();
    }

    @Test
    @DisplayName("Should require FINISHED status")
    void testValidateStatus() {
        assertEquals(GameStatus.FINISHED, chain.validateStatus());
    }

    @Test
    @DisplayName("Should allow RESTART_DISAGREE when opponent responds")
    void testValidateOpponentDisagrees() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK) // Black disagrees
                .build();

        assertTrue(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should reject RESTART_DISAGREE when no pending proposal")
    void testValidateWithNoPendingProposal() {
        game.setRestartProposerColor(null);

        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(100L)
                .color(PlayerColor.BLACK)
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should reject RESTART_DISAGREE when proposer tries to disagree with own proposal")
    void testValidateProposerCannotDisagree() {
        GameAction action = GameAction.builder()
                .type(ActionType.RESTART_DISAGREE)
                .playerId(200L)
                .color(PlayerColor.WHITE) // Proposer tries to disagree
                .build();

        assertFalse(chain.validate(game, action));
    }

    @Test
    @DisplayName("Should return correct action types")
    void testGetActionTypes() {
        assertEquals(1, chain.getActionTypes().size());
        assertTrue(chain.getActionTypes().contains(ActionType.RESTART_DISAGREE));
    }

    @Test
    @DisplayName("Should return correct sort order")
    void testSort() {
        assertEquals(1, chain.sort());
    }
}
