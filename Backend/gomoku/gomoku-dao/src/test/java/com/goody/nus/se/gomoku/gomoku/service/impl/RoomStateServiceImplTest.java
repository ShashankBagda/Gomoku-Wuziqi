package com.goody.nus.se.gomoku.gomoku.service.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RoomStateServiceImpl
 *
 * <p>Test coverage:
 * <ul>
 *   <li>Initial game state creation</li>
 *   <li>Random color assignment</li>
 *   <li>Idempotency (prevent duplicate creation)</li>
 *   <li>Race condition handling</li>
 *   <li>Error handling</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RoomStateServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private RoomStateServiceImpl roomStateService;

    private Long testRoomId;
    private Long testPlayer1Id;
    private Long testPlayer2Id;

    @BeforeEach
    void setUp() {
        testRoomId = 1001L;
        testPlayer1Id = 2001L;
        testPlayer2Id = 2002L;
    }

    /**
     * Test: Successfully initialize a new game state
     *
     * <p>Verify that:
     * <ul>
     *   <li>GameDocument is created with correct roomId</li>
     *   <li>Both players are assigned (one black, one white)</li>
     *   <li>Initial status is WAITING</li>
     *   <li>Ready flags are false</li>
     *   <li>Document is persisted to MongoDB</li>
     * </ul>
     */
    @Test
    void initializeGameState_Success_CreatesNewDocument() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument savedDocument = GameDocument.builder()
                .roomId(testRoomId)
                .blackPlayerId(testPlayer1Id)
                .whitePlayerId(testPlayer2Id)
                .blackReady(false)
                .whiteReady(false)
                .status(GameStatus.WAITING)
                .version(0L)
                .build();

        when(gameRepository.save(any(GameDocument.class))).thenReturn(savedDocument);

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "RANKED");

        // Assert
        assertNotNull(result);
        assertEquals(testRoomId, result.getRoomId());
        assertEquals(GameStatus.WAITING, result.getStatus());
        assertFalse(result.getBlackReady());
        assertFalse(result.getWhiteReady());

        // Verify both players are assigned (one is black, other is white)
        assertTrue((result.getBlackPlayerId().equals(testPlayer1Id) && result.getWhitePlayerId().equals(testPlayer2Id))
                || (result.getBlackPlayerId().equals(testPlayer2Id) && result.getWhitePlayerId().equals(testPlayer1Id)));

        // Verify repository interactions
        verify(gameRepository).findByRoomId(testRoomId);
        verify(gameRepository).save(any(GameDocument.class));
    }

    /**
     * Test: Idempotency - return existing document if already initialized
     *
     * <p>Verify that:
     * <ul>
     *   <li>If game state already exists, return it without creating a new one</li>
     *   <li>No save operation is performed</li>
     * </ul>
     */
    @Test
    void initializeGameState_Idempotency_ReturnsExistingDocument() {
        // Arrange
        GameDocument existingDocument = GameDocument.builder()
                .roomId(testRoomId)
                .blackPlayerId(testPlayer1Id)
                .whitePlayerId(testPlayer2Id)
                .blackReady(false)
                .whiteReady(false)
                .status(GameStatus.WAITING)
                .version(0L)
                .build();

        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.of(existingDocument));

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        // Assert
        assertNotNull(result);
        assertEquals(testRoomId, result.getRoomId());
        assertSame(existingDocument, result); // Should return the same instance

        // Verify no save operation
        verify(gameRepository).findByRoomId(testRoomId);
        verify(gameRepository, never()).save(any(GameDocument.class));
    }

    /**
     * Test: Handle race condition with DuplicateKeyException
     *
     * <p>Scenario: Two threads try to initialize the same room simultaneously.
     * One succeeds, the other gets DuplicateKeyException.
     *
     * <p>Verify that:
     * <ul>
     *   <li>DuplicateKeyException is caught</li>
     *   <li>Existing document is fetched and returned</li>
     *   <li>No exception is thrown to caller</li>
     * </ul>
     */
    @Test
    void initializeGameState_RaceCondition_HandlesGracefully() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId))
                .thenReturn(Optional.empty()) // First check: not exists
                .thenReturn(Optional.of(GameDocument.builder() // Second check: exists (after race)
                        .roomId(testRoomId)
                        .blackPlayerId(testPlayer1Id)
                        .whitePlayerId(testPlayer2Id)
                        .status(GameStatus.WAITING)
                        .build()));

        when(gameRepository.save(any(GameDocument.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "PRIVATE");

        // Assert
        assertNotNull(result);
        assertEquals(testRoomId, result.getRoomId());

        // Verify findByRoomId called twice (initial check + recovery)
        verify(gameRepository, times(2)).findByRoomId(testRoomId);
        verify(gameRepository).save(any(GameDocument.class));
    }

    /**
     * Test: Handle save failure with generic exception
     *
     * <p>Verify that:
     * <ul>
     *   <li>Generic exceptions are caught and wrapped in BizException</li>
     *   <li>Proper error code is set</li>
     * </ul>
     */
    @Test
    void initializeGameState_SaveFailure_ThrowsBizException() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());
        when(gameRepository.save(any(GameDocument.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        BizException exception = assertThrows(BizException.class,
                () -> roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL"));

        assertEquals(ErrorCodeEnum.UNKNOWN_ERROR, exception.getErrorCode());
        assertNotNull(exception.getArgs());
        assertTrue(exception.getArgs().length > 0);
        assertTrue(exception.getArgs()[0].toString().contains("Failed to initialize game state"));

        verify(gameRepository).findByRoomId(testRoomId);
        verify(gameRepository).save(any(GameDocument.class));
    }

    /**
     * Test: Handle race condition but recovery fails
     *
     * <p>Scenario: DuplicateKeyException occurs, but fetching existing document also fails.
     *
     * <p>Verify that:
     * <ul>
     *   <li>BizException is thrown with appropriate error code</li>
     * </ul>
     */
    @Test
    void initializeGameState_RaceConditionRecoveryFails_ThrowsBizException() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId))
                .thenReturn(Optional.empty()) // First check: not exists
                .thenReturn(Optional.empty()); // Second check: still not found (weird race condition)

        when(gameRepository.save(any(GameDocument.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // Act & Assert
        BizException exception = assertThrows(BizException.class,
                () -> roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "RANKED"));

        assertEquals(ErrorCodeEnum.UNKNOWN_ERROR, exception.getErrorCode());
        assertNotNull(exception.getArgs());
        assertTrue(exception.getArgs().length > 0);
        assertTrue(exception.getArgs()[0].toString().contains("Failed to initialize or fetch game state"));

        verify(gameRepository, times(2)).findByRoomId(testRoomId);
        verify(gameRepository).save(any(GameDocument.class));
    }

    /**
     * Test: Verify players are correctly assigned to colors
     *
     * <p>Verify that:
     * <ul>
     *   <li>One player is black, the other is white</li>
     *   <li>No player is left unassigned</li>
     * </ul>
     */
    @Test
    void initializeGameState_PlayerColorAssignment_BothPlayersAssigned() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "PRIVATE");

        // Assert
        assertNotNull(result.getBlackPlayerId());
        assertNotNull(result.getWhitePlayerId());
        assertNotEquals(result.getBlackPlayerId(), result.getWhitePlayerId());

        // Verify both players are assigned
        assertTrue(
                (result.getBlackPlayerId().equals(testPlayer1Id) && result.getWhitePlayerId().equals(testPlayer2Id))
                        || (result.getBlackPlayerId().equals(testPlayer2Id) && result.getWhitePlayerId().equals(testPlayer1Id)),
                "Both players should be assigned, one as black and one as white"
        );
    }

    /**
     * Test: Verify mode type is correctly stored
     *
     * <p>Verify that:
     * <ul>
     *   <li>Mode type (CASUAL, RANKED, PRIVATE) is properly stored</li>
     * </ul>
     */
    @Test
    void initializeGameState_ModeType_CasualMode() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        // Assert
        assertNotNull(result);
        assertEquals("CASUAL", capturedDocument[0].getModeType());
    }

    /**
     * Test: Verify mode type is correctly stored for RANKED mode
     */
    @Test
    void initializeGameState_ModeType_RankedMode() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "RANKED");

        // Assert
        assertNotNull(result);
        assertEquals("RANKED", capturedDocument[0].getModeType());
    }

    /**
     * Test: Verify mode type is correctly stored for PRIVATE mode
     */
    @Test
    void initializeGameState_ModeType_PrivateMode() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "PRIVATE");

        // Assert
        assertNotNull(result);
        assertEquals("PRIVATE", capturedDocument[0].getModeType());
    }

    /**
     * Test: Verify initial game state fields are correctly set
     */
    @Test
    void initializeGameState_InitialFields_AreCorrectlySet() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        // Assert
        GameDocument doc = capturedDocument[0];
        assertNotNull(doc);
        assertEquals(testRoomId, doc.getRoomId());
        assertFalse(doc.getBlackReady());
        assertFalse(doc.getWhiteReady());
        assertEquals(GameStatus.WAITING, doc.getStatus());
        assertNull(doc.getDrawProposerColor());
        assertNull(doc.getUndoProposerColor());
        assertNull(doc.getLastAction());
        assertNotNull(doc.getActionHistory());
        assertTrue(doc.getActionHistory().isEmpty());
        assertEquals(0L, doc.getVersion());
        assertNotNull(doc.getCreateTime());
        assertNotNull(doc.getUpdateTime());
        assertNotNull(doc.getCurrentState());
    }

    /**
     * Test: Verify empty game state snapshot is created
     */
    @Test
    void initializeGameState_CurrentState_IsEmpty() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        // Assert
        assertNotNull(result.getCurrentState());
        assertNotNull(result.getCurrentState().getBoard());
        assertEquals(0, result.getCurrentState().getTotalMoves());
    }

    /**
     * Test: Verify timestamps are set correctly
     */
    @Test
    void initializeGameState_Timestamps_AreSet() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        long beforeTime = System.currentTimeMillis();

        GameDocument[] capturedDocument = new GameDocument[1];
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> {
            capturedDocument[0] = invocation.getArgument(0);
            return capturedDocument[0];
        });

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        long afterTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());
        assertTrue(result.getCreateTime() >= beforeTime && result.getCreateTime() <= afterTime);
        assertTrue(result.getUpdateTime() >= beforeTime && result.getUpdateTime() <= afterTime);
        assertEquals(result.getCreateTime(), result.getUpdateTime());
    }

    /**
     * Test: Multiple calls with different roomIds should create different documents
     */
    @Test
    void initializeGameState_DifferentRoomIds_CreateSeparateDocuments() {
        // Arrange
        Long roomId1 = 1001L;
        Long roomId2 = 1002L;

        when(gameRepository.findByRoomId(roomId1)).thenReturn(Optional.empty());
        when(gameRepository.findByRoomId(roomId2)).thenReturn(Optional.empty());

        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GameDocument result1 = roomStateService.initializeGameState(roomId1, testPlayer1Id, testPlayer2Id, "CASUAL");
        GameDocument result2 = roomStateService.initializeGameState(roomId2, testPlayer1Id, testPlayer2Id, "RANKED");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(roomId1, result1.getRoomId());
        assertEquals(roomId2, result2.getRoomId());
        verify(gameRepository, times(2)).save(any(GameDocument.class));
    }

    /**
     * Test: Color assignment uses timestamp for randomness (even timestamp)
     */
    @Test
    void initializeGameState_ColorAssignment_RandomnessVerification() {
        // Arrange
        when(gameRepository.findByRoomId(testRoomId)).thenReturn(Optional.empty());

        // We can't control the exact timestamp, but we can verify both scenarios are possible
        // by running the test multiple times conceptually, or just verify the assignment is valid
        when(gameRepository.save(any(GameDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GameDocument result = roomStateService.initializeGameState(testRoomId, testPlayer1Id, testPlayer2Id, "CASUAL");

        // Assert
        // Verify that one player is black and the other is white
        boolean player1IsBlack = result.getBlackPlayerId().equals(testPlayer1Id);
        boolean player2IsWhite = result.getWhitePlayerId().equals(testPlayer2Id);
        boolean player1IsWhite = result.getWhitePlayerId().equals(testPlayer1Id);
        boolean player2IsBlack = result.getBlackPlayerId().equals(testPlayer2Id);

        // Either player1 is black and player2 is white, or vice versa
        assertTrue((player1IsBlack && player2IsWhite) || (player1IsWhite && player2IsBlack));
    }
}
