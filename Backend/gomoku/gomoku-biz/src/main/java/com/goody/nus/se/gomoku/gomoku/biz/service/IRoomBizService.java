package com.goody.nus.se.gomoku.gomoku.biz.service;

import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CreateRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;

/**
 * Room business service interface
 *
 * <p>Orchestrates room operations with business validations and game module coordination.
 * Follows high cohesion and low coupling principles by:
 * <ul>
 *   <li>High cohesion: All methods focus on room-related business logic</li>
 *   <li>Low coupling: Delegates core room operations to RoomCodeService</li>
 *   <li>Business layer: Handles cross-cutting concerns like queue validation</li>
 * </ul>
 *
 * @author Claude, Haotian
 * @version 1.0, 2025/10/21
 */
public interface IRoomBizService {
    /**
     * Create a private room with queue validation
     *
     * <p>This method orchestrates:
     * <ol>
     *   <li>Validates player is not in match queue</li>
     *   <li>Creates room code via room service</li>
     * </ol>
     *
     * @param playerId player ID who is creating the room
     * @return create room response containing room code
     * @throws com.goody.nus.se.gomoku.common.exception.BizException if player is in match queue
     */
    CreateRoomResponse createRoom(String playerId);

    /**
     * Join a private room with queue validation
     *
     * <p>This method orchestrates:
     * <ol>
     *   <li>Validates player is not in match queue</li>
     *   <li>Joins room via room service</li>
     * </ol>
     *
     * @param request  join room request containing room code
     * @param playerId player ID who is joining
     * @return join room response with status and room info
     * @throws com.goody.nus.se.gomoku.common.exception.BizException if player is in match queue
     */
    JoinRoomResponse joinRoom(JoinRoomRequest request, String playerId);

    /**
     * Leave room and handle game state
     *
     * <p>This method orchestrates:
     * <ol>
     *   <li>Query roomId to check if game is active</li>
     *   <li>Sending a SURRENDER action to the game module if the game is active</li>
     *   <li>Leaving the room in the room module</li>
     * </ol>
     *
     * @param request  leave room request containing room code
     * @param playerId player ID who is leaving
     * @return leave room response
     */
    LeaveRoomResponse leaveRoom(LeaveRoomRequest request, String playerId);
}
