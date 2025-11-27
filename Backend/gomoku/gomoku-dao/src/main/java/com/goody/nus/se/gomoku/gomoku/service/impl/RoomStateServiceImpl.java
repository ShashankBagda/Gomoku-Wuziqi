package com.goody.nus.se.gomoku.gomoku.service.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.model.GameStateSnapshot;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameRepository;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IRoomStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Room state management service implementation
 *
 * <p>This service handles the initialization of game state in MongoDB when
 * two players are successfully matched or join a room together.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Create and persist initial GameDocument in MongoDB</li>
 *   <li>Randomly assign player colors (black/white)</li>
 *   <li>Set up initial game state (WAITING status)</li>
 *   <li>Handle idempotency (prevent duplicate initialization)</li>
 * </ul>
 *
 * @author Generated
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStateServiceImpl implements IRoomStateService {

    private final GameRepository gameRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Implementation details:
     * <ul>
     *   <li>Uses timestamp parity for random color assignment</li>
     *   <li>Idempotent: If document already exists, returns existing document</li>
     *   <li>Atomic: MongoDB's unique index on roomId prevents race conditions</li>
     * </ul>
     *
     * @throws BizException with INTERNAL_ERROR if persistence fails unexpectedly
     */
    @Override
    public GameDocument initializeGameState(Long roomId, Long player1Id, Long player2Id, String modeType) {
        // Check if game state already exists (idempotency)
        GameDocument existingGame = gameRepository.findByRoomId(roomId).orElse(null);
        if (existingGame != null) {
            log.info("[RoomState] Game state already exists for roomId={}, returning existing document", roomId);
            return existingGame;
        }

        // Randomly assign colors: use timestamp for randomness
        long now = System.currentTimeMillis();
        boolean player1IsBlack = (now % 2) == 0;

        // Build initial game document
        GameDocument gameDocument = GameDocument.builder()
                .roomId(roomId)
                .blackPlayerId(player1IsBlack ? player1Id : player2Id)
                .whitePlayerId(player1IsBlack ? player2Id : player1Id)
                .blackReady(false)
                .whiteReady(false)
                .currentState(GameStateSnapshot.createEmpty())
                .lastAction(null)
                .actionHistory(new ArrayList<>())
                .version(0L)
                .createTime(now)
                .updateTime(now)
                .status(GameStatus.WAITING)
                .drawProposerColor(null)
                .undoProposerColor(null)
                .modeType(modeType)
                .build();

        try {
            // Persist to MongoDB
            GameDocument savedDocument = gameRepository.save(gameDocument);

            log.info("[RoomState] Initialized game state: roomId={}, black={}, white={}, status={}",
                    roomId, savedDocument.getBlackPlayerId(), savedDocument.getWhitePlayerId(),
                    savedDocument.getStatus());

            return savedDocument;
        } catch (DuplicateKeyException e) {
            // Handle race condition: another thread already created the document
            log.warn("[RoomState] Race condition detected for roomId={}, fetching existing document", roomId);
            return gameRepository.findByRoomId(roomId)
                    .orElseThrow(() -> new BizException(ErrorCodeEnum.UNKNOWN_ERROR,
                            "Failed to initialize or fetch game state for roomId=" + roomId));
        } catch (Exception e) {
            log.error("[RoomState] Failed to initialize game state for roomId={}", roomId, e);
            throw new BizException(ErrorCodeEnum.UNKNOWN_ERROR,
                    "Failed to initialize game state: " + e.getMessage());
        }
    }
}
