package com.goody.nus.se.gomoku.gomoku.game.service.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.GameStateResponse;
import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.enums.PlayerColor;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.chain.execute.ExecuteChainHandler;
import com.goody.nus.se.gomoku.gomoku.game.chain.validate.ValidateChainHandler;
import com.goody.nus.se.gomoku.gomoku.game.service.IGameService;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.mongo.repository.GameRepository;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Game service implementation using Chain of Responsibility pattern
 *
 * <p>This service handles Gomoku game logic with the following design:
 * <ul>
 *   <li>Action + Snapshot Pattern: Separates game actions from state snapshots</li>
 *   <li>Chain of Responsibility: Modular validation and processing handlers</li>
 *   <li>MongoDB Storage: Persistent game state with roomId as key</li>
 *   <li>Ready Mechanism: Two-phase game start (WAITING -> PLAYING)</li>
 * </ul>
 *
 * @author Goody
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements IGameService {

    private final GameRepository gameRepository;
    private final ValidateChainHandler validateChainHandler;
    private final ExecuteChainHandler executeChainHandler;
    private final IGameRoomService gameRoomService;
    private final RoomCodeDao roomCodeDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public GameStateResponse executeAction(Long roomId, Long playerId, GomokuActionRequest request) {
        // 1. Check if room exists in database
        GameRoomDTO roomDTO = gameRoomService.findById(roomId);
        if (roomDTO == null) {
            throw new BizException(ErrorCodeEnum.GAME_NOT_FOUND, roomId);
        }

        // Allow RESTART-related actions even when room is FINISHED
        boolean isRestartAction = request.getType() == ActionType.RESTART
                || request.getType() == ActionType.RESTART_AGREE
                || request.getType() == ActionType.RESTART_DISAGREE;

        if (!isRestartAction && roomDTO.getStatus() == RoomStatusEnum.FINISHED.getValue()) {
            throw new BizException(ErrorCodeEnum.GAME_NOT_FOUND, roomId);
        }

        // 2. Extend room code TTL to keep room active during gameplay
        final boolean roomCodeExist = roomDTO.getRoomCode() != null && roomCodeDao.exists(roomDTO.getRoomCode());
        if (!roomCodeExist) {
            throw new BizException(ErrorCodeEnum.ROOM_NOT_FOUND, roomId);
        }
        roomCodeDao.updateRoomTTL(roomDTO.getRoomCode(), 20);
        log.debug("Extended room code TTL: roomCode={}, roomId={}", roomDTO.getRoomCode(), roomId);

        // 3. Load game document from MongoDB
        GameDocument game = gameRepository.findByRoomId(roomId)
                // create new game if not found (first action in room)
                // Default to CASUAL for fallback cases
                .orElse(GameDocument.createNewGameWithRandomBlack(roomId, playerId, "CASUAL"));

        // 4. Validate player belongs to game
        validatePlayer(game, playerId);

        // 5. Build action
        GameAction action = buildAction(request, playerId, game);

        // 6. Execute validation chain
        boolean validate = true;
        if (action.getType() != ActionType.SURRENDER) {
            validate = validateChainHandler.handle(game, action);
        }
        if (!validate) {
            throw new BizException(ErrorCodeEnum.INVALID_GAME_ACTION, action);
        }

        // 7. Execute execute chain
        executeChainHandler.handle(game, action);

        // 8. Save to MongoDB
        gameRepository.save(game);

        // 9. Return response
        return toResponse(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameStateResponse getState(Long roomId, Long playerId) {
        GameDocument game = gameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new BizException(ErrorCodeEnum.GAME_NOT_FOUND, roomId));

        validatePlayer(game, playerId);

        return toResponse(game);
    }

    /**
     * Build GameAction object from request
     *
     * <p>Enriches the frontend request with:
     * <ul>
     *   <li>Player color (determined by player ID)</li>
     *   <li>Current timestamp (for timeout detection)</li>
     * </ul>
     *
     * @param request  Frontend action request (type + optional position)
     * @param playerId Player ID
     * @param game     Current game document
     * @return Complete GameAction ready for processing
     */
    private GameAction buildAction(GomokuActionRequest request, Long playerId, GameDocument game) {
        return GameAction.builder()
                .type(request.getType())
                .playerId(playerId)
                .position(request.getPosition())
                .color(getPlayerColor(game, playerId))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Validate that player belongs to this game
     *
     * @param game     Game document
     * @param playerId Player ID to validate
     * @throws BizException if player is not black or white player in this game
     */
    private void validatePlayer(GameDocument game, Long playerId) {
        if (game.getStatus() == GameStatus.WAITING) {
            // allow any player to join when waiting
            return;
        }

        boolean isValidPlayer = playerId.equals(game.getBlackPlayerId()) ||
                playerId.equals(game.getWhitePlayerId());

        if (!isValidPlayer) {
            throw new BizException(ErrorCodeEnum.PLAYER_NOT_IN_GAME, playerId, game.getRoomId());
        }
    }

    /**
     * Get player color by player ID
     *
     * @param game     Game document
     * @param playerId Player ID
     * @return PlayerColor (BLACK or WHITE)
     * @throws BizException if player is not in this game
     */
    private PlayerColor getPlayerColor(GameDocument game, Long playerId) {
        if (playerId.equals(game.getBlackPlayerId())) {
            return PlayerColor.BLACK;
        } else if (playerId.equals(game.getWhitePlayerId())) {
            return PlayerColor.WHITE;
        } else if (game.getStatus() == GameStatus.WAITING && game.getBlackPlayerId() == null) {
            return PlayerColor.BLACK;
        } else if (game.getStatus() == GameStatus.WAITING && game.getWhitePlayerId() == null) {
            return PlayerColor.WHITE;
        }
        throw new BizException(ErrorCodeEnum.PLAYER_NOT_IN_GAME, playerId, game.getRoomId());
    }

    /**
     * Convert GameDocument to GameStateResponse
     *
     * @param game Game document
     * @return GameStateResponse
     */
    private GameStateResponse toResponse(GameDocument game) {
        return GameStateResponse.builder()
                .roomId(game.getRoomId())
                .blackPlayerId(game.getBlackPlayerId())
                .whitePlayerId(game.getWhitePlayerId())
                .blackReady(game.getBlackReady())
                .whiteReady(game.getWhiteReady())
                .currentState(game.getCurrentState())
                .lastAction(game.getLastAction())
                .actionHistory(game.getActionHistory())
                .version(game.getVersion())
                .createTime(game.getCreateTime())
                .updateTime(game.getUpdateTime())
                .status(game.getStatus())
                .drawProposerColor(game.getDrawProposerColor())
                .modeType(game.getModeType())
                .build();
    }
}
