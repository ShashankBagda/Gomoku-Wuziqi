package com.goody.nus.se.gomoku.gomoku.biz.service.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CreateRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;
import com.goody.nus.se.gomoku.gomoku.biz.service.IRoomBizService;
import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.game.service.IGameService;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.Service.RoomCodeService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Room business service implementation
 *
 * <p>Orchestrates room operations with business validations and game module coordination.
 * Follows high cohesion and low coupling principles:
 * <ul>
 *   <li>High cohesion: All methods focus on room-related business orchestration</li>
 *   <li>Low coupling: Delegates to specialized services (RoomCodeService, IMatchService, IGameService)</li>
 *   <li>Business layer: Handles cross-cutting concerns like queue validation before room operations</li>
 * </ul>
 *
 * @author Claude, Haotian
 * @version 1.0, 2025/10/21
 */
@Slf4j
@Service
public class RoomBizServiceImpl implements IRoomBizService {

    @Autowired
    private RoomCodeService roomCodeService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IGameRoomService gameRoomService;

    @Autowired
    private IMatchService matchService;

    /**
     * {@inheritDoc}
     *
     * <p>Implementation validates player is not in match queue before creating room.
     * Ensures mutual exclusion between private rooms and match queues.
     */
    @Override
    public CreateRoomResponse createRoom(String playerId) {
        log.info("[RoomBiz] Processing create room request for player {}", playerId);

        // Validate player is not in match queue (business validation)
        validateNotInQueue(playerId, "create room");

        // Delegate to room service for room creation
        String roomCode = roomCodeService.createRoom();

        log.info("[RoomBiz] Player {} created private room: {}", playerId, roomCode);
        return new CreateRoomResponse(roomCode);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation validates player is not in match queue before joining room.
     * Ensures mutual exclusion between private rooms and match queues.
     */
    @Override
    public JoinRoomResponse joinRoom(JoinRoomRequest request, String playerId) {
        log.info("[RoomBiz] Processing join room request for player {} to room {}",
                playerId, request.getRoomCode());

        // Validate player is not in match queue (business validation)
        validateNotInQueue(playerId, "join room");

        // Delegate to room service for room join logic
        JoinRoomResponse response = roomCodeService.joinRoom(request, playerId);

        log.info("[RoomBiz] Player {} joined room {} with status: {}",
                playerId, request.getRoomCode(), response.getStatus());
        return response;
    }

    /**
     * Validate that player is not in any match queue
     *
     * <p>Helper method to encapsulate queue validation logic.
     * Throws BizException if player is found in a queue.
     *
     * @param playerId   player ID to validate
     * @param operation  operation name for error message (e.g., "create room", "join room")
     * @throws BizException if player is in match queue
     */
    private void validateNotInQueue(String playerId, String operation) {
        String queueMode = matchService.findPlayerQueue(playerId);
        if (queueMode != null) {
            log.warn("[RoomBiz] Player {} cannot {} while in {} queue", playerId, operation, queueMode);
            throw new BizException(ErrorCodeEnum.PLAYER_IN_MATCH_QUEUE, queueMode);
        }
    }

    /**
     * Leave room and handle game state
     *
     * <p>This method orchestrates:
     * <ol>
     *   <li>Query roomId by room code to check if game is active</li>
     *   <li>If game is active, send a SURRENDER action to the game module</li>
     *   <li>Leave the room in the room module</li>
     * </ol>
     *
     * @param request leave room request containing room code
     * @param playerId player ID who is leaving
     * @return leave room response
     */
    @Override
    public LeaveRoomResponse leaveRoom(LeaveRoomRequest request, String playerId) {
        String roomCode = request.getRoomCode();
        Long roomId = null;

        try {
            roomId = gameRoomService.findRoomIdByRoomCode(roomCode);
        } catch (Exception e) {
            log.warn("[RoomBiz] Room {} not found when leaving", roomCode);
        }

        boolean isFinished = false;
        if (roomId != null) {
            try {
                var roomDTO = gameRoomService.findById(roomId);
                if (roomDTO != null && roomDTO.getStatus() == RoomStatusEnum.FINISHED.getValue()) {
                    isFinished = true;
                }
            } catch (Exception e) {
                log.warn("[RoomBiz] Unable to query room {} status: {}", roomCode, e.getMessage());
            }
        }

        // Only send SURRENDER when the game is not finished
        if (roomId != null && !isFinished) {
            try {
                GomokuActionRequest surrenderRequest = GomokuActionRequest.builder()
                        .type(ActionType.SURRENDER)
                        .build();
                log.info("[RoomBiz] Player {} leaving active room {}, sending SURRENDER", playerId, roomCode);
                gameService.executeAction(roomId, Long.parseLong(playerId), surrenderRequest);
            } catch (BizException e) {
                log.warn("[RoomBiz] Ignore SURRENDER failure for player {} in room {}: {}",
                        playerId, roomCode, e.getMessage());
            }
        } else {
            log.info("[RoomBiz] Player {} leaving room {} which is already finished or inactive", playerId, roomCode);
        }

        // Proceed to leave room (always)
        LeaveRoomResponse response = roomCodeService.leaveRoom(request, playerId);

        log.info("[RoomBiz] Player {} successfully left room {} with status: {}",
                playerId, roomCode, response.getStatus());
        return response;
    }
}
