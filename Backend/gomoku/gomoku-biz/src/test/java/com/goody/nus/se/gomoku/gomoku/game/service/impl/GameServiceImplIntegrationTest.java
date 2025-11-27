package com.goody.nus.se.gomoku.gomoku.game.service.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.GameStateResponse;
import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.TestApplication;
import com.goody.nus.se.gomoku.gomoku.game.service.IGameService;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameHistoryDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameHistoryRepository;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameRepository;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive Integration Test for GameServiceImpl
 * Tests complete game flow with real Spring Boot context and all chains
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@SpringBootTest(classes = TestApplication.class)
@ContextConfiguration(classes = {TestApplication.class})
class GameServiceImplIntegrationTest {

    private static final Long ROOM_ID = 9001L;
    private static final Long PLAYER_ID_1 = 1001L;
    private static final Long PLAYER_ID_2 = 1002L;
    private static final String ROOM_CODE = "TEST-ROOM-9001";
    @Autowired
    private IGameService gameService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameHistoryRepository gameHistoryRepository;
    @Autowired
    private IGameRoomService gameRoomService;
    @Autowired
    private com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao roomCodeDao;

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        gameRepository.findByRoomId(ROOM_ID).ifPresent(game -> gameRepository.delete(game));
        gameHistoryRepository.deleteByRoomId(ROOM_ID);

        // Clean up room code in Redis/cache if exists
        if (roomCodeDao.exists(ROOM_CODE)) {
            roomCodeDao.deleteRoom(ROOM_CODE);
        }

        // Create room record in MySQL (required for GameService validation)
        GameRoomDTO roomDTO = GameRoomDTO.builder()
                .id(ROOM_ID)
                .roomCode(ROOM_CODE)
                .player1Id(PLAYER_ID_1)
                .player2Id(PLAYER_ID_2)
                .status(RoomStatusEnum.MATCHED.getValue())
                .type((byte) 0) // Casual game
                .build();

        // Delete existing record if any
        if (gameRoomService.findById(ROOM_ID) != null) {
            gameRoomService.deleteById(ROOM_ID);
        }

        // Save room record
        gameRoomService.save(roomDTO);

        // Create room code in Redis/cache
        roomCodeDao.createRoomCode(ROOM_CODE, 30);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        gameRepository.findByRoomId(ROOM_ID).ifPresent(game -> gameRepository.delete(game));
        gameHistoryRepository.deleteByRoomId(ROOM_ID);
        gameRoomService.deleteById(ROOM_ID);

        // Clean up room code in Redis/cache
        if (roomCodeDao.exists(ROOM_CODE)) {
            roomCodeDao.deleteRoom(ROOM_CODE);
        }
    }

    // ==================== Complete Game Flow Test ====================

    @Test
    @DisplayName("Complete Game Flow: Ready, BlackId First")
    void test_Ready_BlackIdFirst() {
        // Step 1: Black player marks ready (creates new game)
        GameStateResponse response1 = markReady(PLAYER_ID_1);

        assertNotNull(response1);
        assertNotNull(response1.getCurrentState());
        assertEquals(GameStatus.WAITING, response1.getStatus());
        // only one player ready
        assertTrue(response1.getBlackReady() || response1.getWhiteReady());
        assertFalse(response1.getBlackReady() && response1.getWhiteReady());

        // Step 1.2: Black player marks ready again (idempotent)
        final BizException bizException_1 = assertThrows(BizException.class, () -> {
            markReady(PLAYER_ID_1);
        });
        assertEquals(ErrorCodeEnum.INVALID_GAME_ACTION, bizException_1.getErrorCode());

        // Step 2: White player marks ready (game starts)
        GameStateResponse response2 = markReady(PLAYER_ID_2);

        assertNotNull(response2);
        assertEquals(GameStatus.PLAYING, response2.getStatus());
        assertTrue(response2.getBlackReady());
        assertTrue(response2.getWhiteReady());

        assertEquals(PlayerColor.BLACK, response2.getCurrentState().getCurrentTurn());

        // Step 2.2: White player marks ready again (idempotent)
        final BizException bizException_2 = assertThrows(BizException.class, () -> {
            markReady(PLAYER_ID_2);
        });
        assertEquals(ErrorCodeEnum.INVALID_GAME_ACTION, bizException_2.getErrorCode());

        final Long whitePlayerId = response2.getWhitePlayerId();

        // Step 3: Validate players assigned correctly
        final BizException bizException_3 = assertThrows(BizException.class, () -> {
            playMove(7, 7, whitePlayerId);
        });
        assertEquals(ErrorCodeEnum.INVALID_GAME_ACTION, bizException_2.getErrorCode());
    }

    @Test
    @DisplayName("Complete Game Flow: From Initialization to Win")
    void testCompleteGameFlowToWin() {
        // Step 1: ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play moves to create a winning condition
        // Black places 5 stones in a row horizontally: (7,7), (7,8), (7,9), (7,10), (7,11)
        Long version1 = getCurrentVersion();
        final GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveResponse(resp1, version1, PlayerColor.WHITE, GameStatus.PLAYING, new Position(7, 7));

        Long version2 = getCurrentVersion();
        final GameStateResponse resp2 = playMove(6, 7, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        Long version3 = getCurrentVersion();
        final GameStateResponse resp3 = playMove(7, 8, blackPlayerId);
        verifyMoveResponse(resp3, version3, PlayerColor.WHITE, GameStatus.PLAYING, new Position(7, 8));

        final GameStateResponse resp4 = playMove(6, 8, whitePlayerId);
        verifyMoveSuccess(resp4, PlayerColor.BLACK);

        final GameStateResponse resp5 = playMove(7, 9, blackPlayerId);
        verifyMoveSuccess(resp5, PlayerColor.WHITE);

        final GameStateResponse resp6 = playMove(6, 9, whitePlayerId);
        verifyMoveSuccess(resp6, PlayerColor.BLACK);

        final GameStateResponse resp7 = playMove(7, 10, blackPlayerId);
        verifyMoveSuccess(resp7, PlayerColor.WHITE);

        final GameStateResponse resp8 = playMove(6, 10, whitePlayerId);
        verifyMoveSuccess(resp8, PlayerColor.BLACK);

        // Step 3: Black wins with the 5th stone in a row
        Long versionBeforeWin = getCurrentVersion();
        GameStateResponse finalResponse = playMove(7, 11, blackPlayerId);

        // Verify game is finished with black as winner
        verifyGameFinished(finalResponse, PlayerColor.BLACK.getValue());

        // Verify version incremented
        GameDocument savedGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertTrue(savedGame.getVersion() > versionBeforeWin);
        assertFalse(savedGame.getActionHistory().isEmpty());
    }

    @Test
    @DisplayName("Complete Game Flow: Draw Proposal and Agreement")
    void testCompleteGameFlowWithDrawAgreement() {
        // Step 1: ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play a few moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        GameStateResponse resp4 = playMove(8, 8, whitePlayerId);
        verifyMoveSuccess(resp4, PlayerColor.BLACK);

        // Step 3: Black proposes draw
        GameStateResponse proposalResponse = proposeDraw(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify draw proposal is saved in database
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getDrawProposerColor());

        // Step 4: White agrees to draw
        GameStateResponse agreeResponse = agreeDraw(whitePlayerId);

        // Verify game ends in draw (winner = 0)
        verifyGameFinished(agreeResponse, 0);

        // Verify draw proposal is cleared after agreement
        GameDocument finalGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertNull(finalGame.getDrawProposerColor());
    }

    @Test
    @DisplayName("Complete Game Flow: Draw Proposal and Disagreement")
    void testCompleteGameFlowWithDrawDisagreement() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play a few moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        // Step 3: Black proposes draw
        GameStateResponse proposalResponse = proposeDraw(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify draw proposal is saved
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getDrawProposerColor());

        // Step 4: White disagrees with draw
        GameStateResponse disagreeResponse = disagreeDraw(whitePlayerId);

        // Verify game continues playing
        assertNotNull(disagreeResponse);
        assertEquals(GameStatus.PLAYING, disagreeResponse.getStatus());

        // Verify draw proposal is cleared after disagreement
        GameDocument gameAfterDisagree = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertNull(gameAfterDisagree.getDrawProposerColor());
        assertEquals(GameStatus.PLAYING, gameAfterDisagree.getStatus());

        // Step 5: Should be able to continue playing
        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);
    }

    @Test
    @DisplayName("Complete Game Flow: Draw Proposal Implicitly Rejected by Move")
    void testCompleteGameFlowWithDrawDisagreement_move() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play a few moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        // Step 3: Black proposes draw
        GameStateResponse proposalResponse = proposeDraw(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify draw proposal is saved
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getDrawProposerColor());

        // Step 4: White implicitly disagrees by making a move instead of responding to draw
        GameStateResponse moveResponse = playMove(8, 8, blackPlayerId);
        verifyMoveSuccess(moveResponse, PlayerColor.WHITE);

        // Verify draw proposal is automatically cleared when white makes a move
        GameDocument gameAfterMove = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertNull(gameAfterMove.getDrawProposerColor(), "Draw proposal should be cleared after opponent makes a move");
        assertEquals(GameStatus.PLAYING, gameAfterMove.getStatus());

        // Step 5: Should be able to continue playing normally
        GameStateResponse resp3 = playMove(8, 7, whitePlayerId);
        verifyMoveSuccess(resp3, PlayerColor.BLACK);
    }

    @Test
    @DisplayName("Complete Game Flow: Surrender")
    void testCompleteGameFlowWithSurrender_black() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play a few moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        // Step 3: Black surrenders
        GameStateResponse surrenderResponse = surrender(blackPlayerId);

        // Verify game ends with white as winner
        verifyGameFinished(surrenderResponse, PlayerColor.WHITE.getValue());
    }

    @Test
    @DisplayName("Complete Game Flow: Surrender")
    void testCompleteGameFlowWithSurrender_white() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play a few moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        // Step 3: Black surrenders
        GameStateResponse surrenderResponse = surrender(whitePlayerId);

        // Verify game ends with white as winner
        verifyGameFinished(surrenderResponse, PlayerColor.BLACK.getValue());
    }

    @Test
    @DisplayName("Complete Game Flow: Undo Proposal and Agreement (Last Move by Proposer - Undo 1 Move)")
    void testCompleteGameFlowWithUndoAgreement_UndoOwnMove() {
        // Scenario: Black just made a mistake at (8,7), wants to undo their own last move
        // Last move was made by Black (proposer) → undo 1 move

        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play 3 moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId); // Black at (7,7)
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId); // White at (7,8)
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId); // Black at (8,7) - Black made a mistake!
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        // Step 3: Verify board state before undo
        GameDocument gameBeforeUndo = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(3, gameBeforeUndo.getCurrentState().getTotalMoves());
        assertEquals(1, gameBeforeUndo.getCurrentState().getBoard()[7][7]); // Black
        assertEquals(2, gameBeforeUndo.getCurrentState().getBoard()[7][8]); // White
        assertEquals(1, gameBeforeUndo.getCurrentState().getBoard()[8][7]); // Black (mistake)
        assertEquals(PlayerColor.WHITE, gameBeforeUndo.getCurrentState().getCurrentTurn());

        // Step 4: Black proposes undo (last move was Black's, so undo 1 move)
        GameStateResponse proposalResponse = proposeUndo(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify undo proposal is saved in database
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getUndoProposerColor());

        // Step 5: White agrees to undo
        GameStateResponse agreeResponse = agreeUndo(whitePlayerId);

        // Verify game continues playing
        assertNotNull(agreeResponse);
        assertEquals(GameStatus.PLAYING, agreeResponse.getStatus());

        // Step 6: Verify only 1 move was reverted (Black's mistake at 8,7)
        GameDocument gameAfterUndo = gameRepository.findByRoomId(ROOM_ID).orElseThrow();

        // Board should have last move removed (8,7)
        assertEquals(0, gameAfterUndo.getCurrentState().getBoard()[8][7], "Position (8,7) should be empty after undo");

        // First 2 moves should still exist
        assertEquals(1, gameAfterUndo.getCurrentState().getBoard()[7][7], "Position (7,7) should still have black stone");
        assertEquals(2, gameAfterUndo.getCurrentState().getBoard()[7][8], "Position (7,8) should still have white stone");

        // Total moves should be reduced by 1
        assertEquals(2, gameAfterUndo.getCurrentState().getTotalMoves(), "Total moves should be 2 after undoing");

        // Current turn should be Black (the proposer gets to replay)
        assertEquals(PlayerColor.BLACK, gameAfterUndo.getCurrentState().getCurrentTurn(),
                "Turn should be Black (proposer) to replay their move");

        // Undo proposal should be cleared
        assertNull(gameAfterUndo.getUndoProposerColor(), "Undo proposal should be cleared after agreement");

        // Verify action history - only 1 MOVE action should be removed
        long moveCount = gameAfterUndo.getActionHistory().stream()
                .filter(a -> a.getType() == ActionType.MOVE)
                .count();
        assertEquals(2, moveCount, "Should have 2 MOVE actions left in history after undo");

        // Step 7: Black should be able to make a different move
        GameStateResponse resp4 = playMove(9, 9, blackPlayerId);
        verifyMoveSuccess(resp4, PlayerColor.WHITE);
    }

    @Test
    @DisplayName("Complete Game Flow: Undo Proposal and Agreement (Last Move by Opponent - Undo 2 Moves)")
    void testCompleteGameFlowWithUndoAgreement_UndoOpponentMove() {
        // Scenario: White just made a good move at (8,8), Black wants to undo both White's move and Black's own previous move
        // Last move was made by White (opponent of Black who is proposing) → undo 2 moves

        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play 4 moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId); // Black at (7,7)
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId); // White at (7,8)
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId); // Black at (8,7)
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        GameStateResponse resp4 = playMove(8, 8, whitePlayerId); // White at (8,8) - Good move!
        verifyMoveSuccess(resp4, PlayerColor.BLACK);

        // Step 3: Verify board state before undo
        GameDocument gameBeforeUndo = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(4, gameBeforeUndo.getCurrentState().getTotalMoves());
        assertEquals(1, gameBeforeUndo.getCurrentState().getBoard()[7][7]); // Black
        assertEquals(2, gameBeforeUndo.getCurrentState().getBoard()[7][8]); // White
        assertEquals(1, gameBeforeUndo.getCurrentState().getBoard()[8][7]); // Black
        assertEquals(2, gameBeforeUndo.getCurrentState().getBoard()[8][8]); // White (good move)
        assertEquals(PlayerColor.BLACK, gameBeforeUndo.getCurrentState().getCurrentTurn());

        // Step 4: Black proposes undo (last move was White's at 8,8, so undo 2 moves)
        GameStateResponse proposalResponse = proposeUndo(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify undo proposal is saved in database
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getUndoProposerColor());

        // Step 5: White agrees to undo
        GameStateResponse agreeResponse = agreeUndo(whitePlayerId);

        // Verify game continues playing
        assertNotNull(agreeResponse);
        assertEquals(GameStatus.PLAYING, agreeResponse.getStatus());

        // Step 6: Verify 2 moves were reverted (White's 8,8 and Black's 8,7)
        GameDocument gameAfterUndo = gameRepository.findByRoomId(ROOM_ID).orElseThrow();

        // Board should have last 2 moves removed
        assertEquals(0, gameAfterUndo.getCurrentState().getBoard()[8][8], "Position (8,8) should be empty after undo");
        assertEquals(0, gameAfterUndo.getCurrentState().getBoard()[8][7], "Position (8,7) should be empty after undo");

        // First 2 moves should still exist
        assertEquals(1, gameAfterUndo.getCurrentState().getBoard()[7][7], "Position (7,7) should still have black stone");
        assertEquals(2, gameAfterUndo.getCurrentState().getBoard()[7][8], "Position (7,8) should still have white stone");

        // Total moves should be reduced by 2
        assertEquals(2, gameAfterUndo.getCurrentState().getTotalMoves(), "Total moves should be 2 after undoing 2 moves");

        // Current turn should be Black (the proposer gets to replay)
        assertEquals(PlayerColor.BLACK, gameAfterUndo.getCurrentState().getCurrentTurn(),
                "Turn should be Black (proposer) to replay");

        // Undo proposal should be cleared
        assertNull(gameAfterUndo.getUndoProposerColor(), "Undo proposal should be cleared after agreement");

        // Verify action history - 2 MOVE actions should be removed
        long moveCount = gameAfterUndo.getActionHistory().stream()
                .filter(a -> a.getType() == ActionType.MOVE)
                .count();
        assertEquals(2, moveCount, "Should have 2 MOVE actions left in history after undo");

        // Step 7: Black should be able to make a different move
        GameStateResponse resp5 = playMove(10, 10, blackPlayerId);
        verifyMoveSuccess(resp5, PlayerColor.WHITE);
    }

    @Test
    @DisplayName("Complete Game Flow: Undo Proposal and Disagreement")
    void testCompleteGameFlowWithUndoDisagreement() {
        // Scenario: Black proposes undo, but White disagrees - game continues with no changes

        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play 3 moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        // Step 3: Black proposes undo
        GameStateResponse proposalResponse = proposeUndo(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify undo proposal is saved
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getUndoProposerColor());

        // Step 4: White disagrees with undo
        GameStateResponse disagreeResponse = disagreeUndo(whitePlayerId);

        // Verify game continues playing
        assertNotNull(disagreeResponse);
        assertEquals(GameStatus.PLAYING, disagreeResponse.getStatus());

        // Step 5: Verify board state unchanged (no moves were reverted)
        GameDocument gameAfterDisagree = gameRepository.findByRoomId(ROOM_ID).orElseThrow();

        // All 3 moves should still be on the board
        assertEquals(1, gameAfterDisagree.getCurrentState().getBoard()[7][7]);
        assertEquals(2, gameAfterDisagree.getCurrentState().getBoard()[7][8]);
        assertEquals(1, gameAfterDisagree.getCurrentState().getBoard()[8][7]);

        // Total moves should still be 3
        assertEquals(3, gameAfterDisagree.getCurrentState().getTotalMoves());

        // Undo proposal should be cleared after disagreement
        assertNull(gameAfterDisagree.getUndoProposerColor(), "Undo proposal should be cleared after disagreement");
        assertEquals(GameStatus.PLAYING, gameAfterDisagree.getStatus());

        // Step 6: Should be able to continue playing
        GameStateResponse resp4 = playMove(8, 8, whitePlayerId);
        verifyMoveSuccess(resp4, PlayerColor.BLACK);
    }

    @Test
    @DisplayName("Complete Game Flow: Undo Proposal Implicitly Rejected by Move")
    void testCompleteGameFlowWithUndoImplicitRejection() {
        // Scenario: Black proposes undo, but White makes a move instead of responding - undo is auto-rejected

        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Step 2: Play 3 moves
        GameStateResponse resp1 = playMove(7, 7, blackPlayerId);
        verifyMoveSuccess(resp1, PlayerColor.WHITE);

        GameStateResponse resp2 = playMove(7, 8, whitePlayerId);
        verifyMoveSuccess(resp2, PlayerColor.BLACK);

        GameStateResponse resp3 = playMove(8, 7, blackPlayerId);
        verifyMoveSuccess(resp3, PlayerColor.WHITE);

        // Step 3: Black proposes undo
        GameStateResponse proposalResponse = proposeUndo(blackPlayerId);
        assertNotNull(proposalResponse);
        assertEquals(GameStatus.PLAYING, proposalResponse.getStatus());

        // Verify undo proposal is saved
        GameDocument gameAfterProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameAfterProposal.getUndoProposerColor());

        // Step 4: White implicitly rejects by making a move instead of responding
        GameStateResponse moveResponse = playMove(8, 8, whitePlayerId);
        verifyMoveSuccess(moveResponse, PlayerColor.BLACK);

        // Step 5: Verify undo proposal is automatically cleared when opponent makes a move
        GameDocument gameAfterMove = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertNull(gameAfterMove.getUndoProposerColor(),
                "Undo proposal should be automatically cleared when opponent makes a move");
        assertEquals(GameStatus.PLAYING, gameAfterMove.getStatus());

        // Step 6: Verify board state unchanged (no undo occurred)
        // All original 3 moves should still exist, plus the new move at (8,8)
        assertEquals(1, gameAfterMove.getCurrentState().getBoard()[7][7]);
        assertEquals(2, gameAfterMove.getCurrentState().getBoard()[7][8]);
        assertEquals(1, gameAfterMove.getCurrentState().getBoard()[8][7]);
        assertEquals(2, gameAfterMove.getCurrentState().getBoard()[8][8]);

        // Total moves should be 4
        assertEquals(4, gameAfterMove.getCurrentState().getTotalMoves());

        // Step 7: Should be able to continue playing normally
        GameStateResponse resp4 = playMove(9, 9, blackPlayerId);
        verifyMoveSuccess(resp4, PlayerColor.WHITE);
    }

    @Test
    @DisplayName("Validation: Cannot move when not your turn")
    void testCannotMoveWhenNotYourTurn() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Black's turn first
        playMove(7, 7, blackPlayerId);

        // Try to move again as black (should fail - it's white's turn)
        assertThrows(BizException.class, () -> {
            playMove(7, 8, blackPlayerId);
        });
    }

    @Test
    @DisplayName("Validation: Cannot place stone on occupied position")
    void testCannotPlaceStoneOnOccupiedPosition() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        // Black places stone at (7, 7)
        playMove(7, 7, blackPlayerId);
        playMove(7, 8, whitePlayerId);

        // Try to place stone at same position (7, 7)
        assertThrows(BizException.class, () -> {
            playMove(7, 7, blackPlayerId);
        });
    }

    @Test
    @DisplayName("Validation: Cannot act after game finished")
    void testCannotActAfterGameFinished() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        playMove(7, 7, blackPlayerId);
        surrender(blackPlayerId);

        // Try to make another move after game finished
        assertThrows(BizException.class, () -> {
            playMove(8, 8, whitePlayerId);
        });
    }

    @Test
    @DisplayName("State Query: Get game state successfully")
    void testGetGameStateSuccessfully() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        playMove(7, 7, blackPlayerId);
        playMove(7, 8, whitePlayerId);

        // Query game state
        GameStateResponse state = gameService.getState(ROOM_ID, blackPlayerId);

        assertNotNull(state);
        assertNotNull(state.getCurrentState());
        assertEquals(GameStatus.PLAYING, state.getStatus());
        assertTrue(state.getBlackReady());
        assertTrue(state.getWhiteReady());
        assertNotNull(state.getLastAction());
        assertEquals(ActionType.MOVE, state.getLastAction().getType());
    }

    @Test
    @DisplayName("State Query: Cannot query game that doesn't exist")
    void testCannotQueryNonExistentGame() {
        assertThrows(BizException.class, () -> {
            gameService.getState(9999L, PLAYER_ID_1);
        });
    }

    @Test
    @DisplayName("Action History: Verify all actions are recorded")
    void testActionHistoryRecorded() {
        // Step 1: Ready both players to start the game
        final GameStateResponse ready = startGame();
        final Long blackPlayerId = ready.getBlackPlayerId();
        final Long whitePlayerId = ready.getWhitePlayerId();

        playMove(7, 7, blackPlayerId);
        playMove(7, 8, whitePlayerId);
        playMove(8, 7, blackPlayerId);
        playMove(8, 8, whitePlayerId);

        // Retrieve game and verify action history
        GameDocument game = gameRepository.findByRoomId(ROOM_ID).orElseThrow();

        assertNotNull(game.getActionHistory());
        assertTrue(game.getActionHistory().size() >= 6); // 2 READY + 4 MOVE actions
        assertEquals(ActionType.READY, game.getActionHistory().get(0).getType());
        assertEquals(ActionType.READY, game.getActionHistory().get(1).getType());
        assertTrue(game.getActionHistory().stream()
                .filter(action -> action.getType() == ActionType.MOVE)
                .count() >= 4);
    }

    // ==================== RESTART Tests ====================

    @Test
    @DisplayName("Complete RESTART Flow: Game -> Restart Agree -> New Game")
    void testCompleteRestartFlow() {
        // Step 1: Start and finish first game
        GameStateResponse gameStarted = startGame();
        Long blackPlayerId = gameStarted.getBlackPlayerId();
        Long whitePlayerId = gameStarted.getWhitePlayerId();

        // Play moves to create a winning condition (Black wins)
        playMove(7, 7, blackPlayerId);
        playMove(6, 7, whitePlayerId);
        playMove(7, 8, blackPlayerId);
        playMove(6, 8, whitePlayerId);
        playMove(7, 9, blackPlayerId);
        playMove(6, 9, whitePlayerId);
        playMove(7, 10, blackPlayerId);
        playMove(6, 10, whitePlayerId);
        GameStateResponse winResponse = playMove(7, 11, blackPlayerId);

        // Verify game finished
        assertEquals(GameStatus.FINISHED, winResponse.getStatus());
        assertEquals(PlayerColor.BLACK.getValue(), winResponse.getCurrentState().getWinner());

        GameDocument finishedGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(1, finishedGame.getGameCount());

        // Step 2: White proposes restart
        GameStateResponse restartProposal = proposeRestart(whitePlayerId);
        assertEquals(GameStatus.FINISHED, restartProposal.getStatus());

        GameDocument gameWithProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.WHITE, gameWithProposal.getRestartProposerColor());

        // Step 3: Black agrees to restart
        GameStateResponse restartAgree = agreeRestart(blackPlayerId);
        assertNotNull(restartAgree);

        // Step 4: Verify game history was archived
        java.util.List<GameHistoryDocument> history = gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(ROOM_ID);
        assertEquals(1, history.size());

        GameHistoryDocument firstGame = history.get(0);
        assertEquals(ROOM_ID, firstGame.getRoomId());
        assertEquals(1, firstGame.getGameNumber());
        assertEquals(blackPlayerId, firstGame.getWinnerId());
        assertEquals("WIN", firstGame.getEndReason());

        // Step 5: Verify game state was reset
        GameDocument resetGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(GameStatus.WAITING, resetGame.getStatus());
        assertEquals(2, resetGame.getGameCount());
        assertFalse(resetGame.getBlackReady());
        assertFalse(resetGame.getWhiteReady());
        assertNull(resetGame.getRestartProposerColor());

        // Step 6: Verify colors were swapped
        assertEquals(whitePlayerId, resetGame.getBlackPlayerId());
        assertEquals(blackPlayerId, resetGame.getWhitePlayerId());

        // Step 7: Start and play second game
        GameStateResponse secondGameStarted = startGame();
        assertEquals(GameStatus.PLAYING, secondGameStarted.getStatus());
        assertEquals(whitePlayerId, secondGameStarted.getBlackPlayerId());
        assertEquals(blackPlayerId, secondGameStarted.getWhitePlayerId());

        // Play moves in second game (colors swapped)
        GameStateResponse move1 = playMove(5, 5, whitePlayerId);
        assertEquals(PlayerColor.WHITE, move1.getCurrentState().getCurrentTurn());
    }

    @Test
    @DisplayName("RESTART Disagree: Game remains finished")
    void testRestartDisagree() {
        // Step 1: Play game to completion
        GameStateResponse gameStarted = startGame();
        Long blackPlayerId = gameStarted.getBlackPlayerId();
        Long whitePlayerId = gameStarted.getWhitePlayerId();

        // Play to finish
        playMove(7, 7, blackPlayerId);
        playMove(6, 7, whitePlayerId);
        playMove(7, 8, blackPlayerId);
        playMove(6, 8, whitePlayerId);
        playMove(7, 9, blackPlayerId);
        playMove(6, 9, whitePlayerId);
        playMove(7, 10, blackPlayerId);
        playMove(6, 10, whitePlayerId);
        playMove(7, 11, blackPlayerId);

        // Step 2: Black proposes restart
        proposeRestart(blackPlayerId);

        GameDocument gameWithProposal = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(PlayerColor.BLACK, gameWithProposal.getRestartProposerColor());

        // Step 3: White disagrees
        GameStateResponse disagreeResponse = disagreeRestart(whitePlayerId);
        assertEquals(GameStatus.FINISHED, disagreeResponse.getStatus());

        // Step 4: Verify game remains finished and proposal is cleared
        GameDocument finalGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(GameStatus.FINISHED, finalGame.getStatus());
        assertEquals(1, finalGame.getGameCount());
        assertNull(finalGame.getRestartProposerColor());

        // Step 5: Verify NO history was archived
        java.util.List<GameHistoryDocument> history = gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(ROOM_ID);
        assertEquals(0, history.size());
    }

    @Test
    @DisplayName("Multiple RESTART cycles: 3 games in same room")
    void testMultipleRestarts() {
        for (int gameNum = 1; gameNum <= 3; gameNum++) {
            // Start game
            GameStateResponse gameStarted = startGame();
            Long currentBlack = gameStarted.getBlackPlayerId();
            Long currentWhite = gameStarted.getWhitePlayerId();

            // Play to win
            playMove(7, 7, currentBlack);
            playMove(6, 7, currentWhite);
            playMove(7, 8, currentBlack);
            playMove(6, 8, currentWhite);
            playMove(7, 9, currentBlack);
            playMove(6, 9, currentWhite);
            playMove(7, 10, currentBlack);
            playMove(6, 10, currentWhite);
            playMove(7, 11, currentBlack);

            // Verify game finished
            GameDocument finishedGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
            assertEquals(GameStatus.FINISHED, finishedGame.getStatus());
            assertEquals(gameNum, finishedGame.getGameCount());

            // If not last game, restart
            if (gameNum < 3) {
                proposeRestart(currentWhite);
                agreeRestart(currentBlack);

                // Verify history count
                java.util.List<GameHistoryDocument> history = gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(ROOM_ID);
                assertEquals(gameNum, history.size());
            }
        }

        // Final verification: 2 games in history, 1 active
        java.util.List<GameHistoryDocument> finalHistory = gameHistoryRepository.findByRoomIdOrderByGameNumberAsc(ROOM_ID);
        assertEquals(2, finalHistory.size());
        assertEquals(1, finalHistory.get(0).getGameNumber());
        assertEquals(2, finalHistory.get(1).getGameNumber());

        GameDocument currentGame = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(3, currentGame.getGameCount());
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to start a game (both players ready)
     *
     * @return GameStateResponse after white player marks ready (game starts)
     */
    private GameStateResponse startGame() {
        markReady(PLAYER_ID_1);
        return markReady(PLAYER_ID_2);
    }

    /**
     * Helper method to mark a player as ready
     *
     * @param playerId ID of the player marking ready
     * @return GameStateResponse after the ready action is executed
     */
    private GameStateResponse markReady(Long playerId) {
        GomokuActionRequest ready = GomokuActionRequest.builder()
                .type(ActionType.READY)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, ready);
    }

    /**
     * Helper method to play a move
     *
     * @param x        x coordinate on the board
     * @param y        y coordinate on the board
     * @param playerId ID of the player making the move
     * @return GameStateResponse after the move is executed
     */
    private GameStateResponse playMove(int x, int y, Long playerId) {
        GomokuActionRequest move = GomokuActionRequest.builder()
                .type(ActionType.MOVE)
                .position(new Position(x, y))
                .build();
        return gameService.executeAction(ROOM_ID, playerId, move);
    }

    /**
     * Helper method to propose a draw
     *
     * @param playerId ID of the player proposing the draw
     * @return GameStateResponse after the draw proposal is executed
     */
    private GameStateResponse proposeDraw(Long playerId) {
        GomokuActionRequest draw = GomokuActionRequest.builder()
                .type(ActionType.DRAW)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, draw);
    }

    /**
     * Helper method to agree to a draw
     *
     * @param playerId ID of the player agreeing to the draw
     * @return GameStateResponse after the draw agreement is executed
     */
    private GameStateResponse agreeDraw(Long playerId) {
        GomokuActionRequest drawAgree = GomokuActionRequest.builder()
                .type(ActionType.DRAW_AGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, drawAgree);
    }

    /**
     * Helper method to disagree to a draw
     *
     * @param playerId ID of the player disagreeing to the draw
     * @return GameStateResponse after the draw disagreement is executed
     */
    private GameStateResponse disagreeDraw(Long playerId) {
        GomokuActionRequest drawDisagree = GomokuActionRequest.builder()
                .type(ActionType.DRAW_DISAGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, drawDisagree);
    }

    /**
     * Helper method to surrender
     *
     * @param playerId ID of the player surrendering
     * @return GameStateResponse after the surrender is executed
     */
    private GameStateResponse surrender(Long playerId) {
        GomokuActionRequest surrender = GomokuActionRequest.builder()
                .type(ActionType.SURRENDER)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, surrender);
    }

    /**
     * Helper method to propose an undo
     *
     * @param playerId ID of the player proposing the undo
     * @return GameStateResponse after the undo proposal is executed
     */
    private GameStateResponse proposeUndo(Long playerId) {
        GomokuActionRequest undo = GomokuActionRequest.builder()
                .type(ActionType.UNDO)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, undo);
    }

    /**
     * Helper method to agree to an undo
     *
     * @param playerId ID of the player agreeing to the undo
     * @return GameStateResponse after the undo agreement is executed
     */
    private GameStateResponse agreeUndo(Long playerId) {
        GomokuActionRequest undoAgree = GomokuActionRequest.builder()
                .type(ActionType.UNDO_AGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, undoAgree);
    }

    /**
     * Helper method to disagree with an undo
     *
     * @param playerId ID of the player disagreeing with the undo
     * @return GameStateResponse after the undo disagreement is executed
     */
    private GameStateResponse disagreeUndo(Long playerId) {
        GomokuActionRequest undoDisagree = GomokuActionRequest.builder()
                .type(ActionType.UNDO_DISAGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, undoDisagree);
    }

    /**
     * Helper method to propose a restart
     *
     * @param playerId ID of the player proposing the restart
     * @return GameStateResponse after the restart proposal is executed
     */
    private GameStateResponse proposeRestart(Long playerId) {
        GomokuActionRequest restart = GomokuActionRequest.builder()
                .type(ActionType.RESTART)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, restart);
    }

    /**
     * Helper method to agree to a restart
     *
     * @param playerId ID of the player agreeing to the restart
     * @return GameStateResponse after the restart agreement is executed
     */
    private GameStateResponse agreeRestart(Long playerId) {
        GomokuActionRequest restartAgree = GomokuActionRequest.builder()
                .type(ActionType.RESTART_AGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, restartAgree);
    }

    /**
     * Helper method to disagree with a restart
     *
     * @param playerId ID of the player disagreeing with the restart
     * @return GameStateResponse after the restart disagreement is executed
     */
    private GameStateResponse disagreeRestart(Long playerId) {
        GomokuActionRequest restartDisagree = GomokuActionRequest.builder()
                .type(ActionType.RESTART_DISAGREE)
                .build();
        return gameService.executeAction(ROOM_ID, playerId, restartDisagree);
    }

    // ==================== Verification Helper Methods ====================

    /**
     * Verify the response and game state after a move action
     * This includes common validations like action history, version increment, current turn switch, etc.
     *
     * @param response         The GameStateResponse after the move
     * @param previousVersion  The version number before this move (null to skip version check)
     * @param expectedNextTurn Expected next player turn color (null to skip turn check)
     * @param expectedStatus   Expected game status (null to skip status check)
     * @param position         The position that was played (null to skip position check)
     */
    private void verifyMoveResponse(GameStateResponse response,
                                    Long previousVersion,
                                    PlayerColor expectedNextTurn,
                                    GameStatus expectedStatus,
                                    Position position) {
        // Basic response validation
        assertNotNull(response);
        assertNotNull(response.getCurrentState());

        // Verify status if provided
        if (expectedStatus != null) {
            assertEquals(expectedStatus, response.getStatus());
        }

        // Verify next turn if provided (only valid when game is still playing)
        if (expectedNextTurn != null && response.getStatus() == GameStatus.PLAYING) {
            assertEquals(expectedNextTurn, response.getCurrentState().getCurrentTurn());
        }

        // Verify last action is recorded
        assertNotNull(response.getLastAction());
        assertEquals(ActionType.MOVE, response.getLastAction().getType());

        // Verify position if provided
        if (position != null && response.getLastAction().getPosition() != null) {
            assertEquals(position.getX(), response.getLastAction().getPosition().getX());
            assertEquals(position.getY(), response.getLastAction().getPosition().getY());
        }

        // Verify database state
        GameDocument game = gameRepository.findByRoomId(ROOM_ID).orElseThrow();

        // Verify action history contains the move
        assertNotNull(game.getActionHistory());
        assertFalse(game.getActionHistory().isEmpty());
        assertTrue(game.getActionHistory().stream()
                .anyMatch(action -> action.getType() == ActionType.MOVE));

        // Verify version incremented if previous version provided
        if (previousVersion != null) {
            assertTrue(game.getVersion() > previousVersion,
                    "Version should increment after move. Previous: " + previousVersion + ", Current: " + game.getVersion());
        }

        // Verify game state consistency between response and database
        assertEquals(response.getStatus(), game.getStatus());
    }

    /**
     * Simplified version verification - verify a move was successful with basic checks
     *
     * @param response         The GameStateResponse after the move
     * @param expectedNextTurn Expected next player turn color
     */
    private void verifyMoveSuccess(GameStateResponse response, PlayerColor expectedNextTurn) {
        verifyMoveResponse(response, null, expectedNextTurn, GameStatus.PLAYING, null);
    }

    /**
     * Get current game version from database
     *
     * @return Current version number
     */
    private Long getCurrentVersion() {
        return gameRepository.findByRoomId(ROOM_ID)
                .map(GameDocument::getVersion)
                .orElse(0L);
    }

    /**
     * Verify game finished state
     *
     * @param response       The GameStateResponse
     * @param expectedWinner Expected winner (PlayerColor.BLACK.getValue(), PlayerColor.WHITE.getValue(), or 0 for draw)
     */
    private void verifyGameFinished(GameStateResponse response, int expectedWinner) {
        assertNotNull(response);
        assertEquals(GameStatus.FINISHED, response.getStatus());
        assertEquals(expectedWinner, response.getCurrentState().getWinner());

        // Verify in database
        GameDocument game = gameRepository.findByRoomId(ROOM_ID).orElseThrow();
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(expectedWinner, game.getCurrentState().getWinner());
    }
}
