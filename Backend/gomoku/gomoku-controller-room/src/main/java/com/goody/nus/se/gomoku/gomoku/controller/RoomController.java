package com.goody.nus.se.gomoku.gomoku.controller;

import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CreateRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;
import com.goody.nus.se.gomoku.gomoku.biz.service.IRoomBizService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Room controller for private room operations
 *
 * <p>Provides REST endpoints for:
 * <ul>
 *   <li>Creating private rooms</li>
 *   <li>Joining private rooms</li>
 *   <li>Leaving rooms</li>
 * </ul>
 *
 * <p>All operations are executed asynchronously using business thread pool.
 * Uses business layer service (IRoomBizService) for queue validation and orchestration.
 *
 * @author LiYuanXing, Haotian
 * @version 1.0, 2025/10/21
 */
@Slf4j
@RestController
@RequestMapping("/lobby")
public class RoomController {
    @Autowired
    private IRoomBizService roomBizService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Create the room code asynchronously
     *
     * <p>Endpoint: POST /api/lobby/create-room
     *
     * <p>Validates that player is not in match queue before creating room.
     *
     * @param playerId Player ID from request header
     * @return CompletionStage with ApiResult containing room code
     * @author LiYuanXing, Haotian
     */
    @PostMapping("/create-room")
    public CompletionStage<ApiResult<CreateRoomResponse>> createRoom(@RequestHeader("X-User-Id") String playerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Create room: playerId={}", playerId);
            CreateRoomResponse response = roomBizService.createRoom(playerId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Join the room with room code asynchronously
     *
     * <p>Endpoint: POST /api/lobby/join-room
     *
     * <p>Validates that player is not in match queue before joining room.
     *
     * @param request  Join room request containing room code
     * @param playerId Player ID from request header
     * @return CompletionStage with ApiResult containing join status and room info
     * @author LiYuanXing, Haotian
     */
    @PostMapping("/join-room")
    public CompletionStage<ApiResult<JoinRoomResponse>> joinRoom(@RequestBody JoinRoomRequest request,
                                                                  @RequestHeader("X-User-Id") String playerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Join room: roomCode={}, playerId={}", request.getRoomCode(), playerId);
            JoinRoomResponse response = roomBizService.joinRoom(request, playerId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Leave the room asynchronously
     *
     * <p>Uses business layer service to orchestrate:
     * <ul>
     *   <li>Sending SURRENDER action to game module if game is active</li>
     *   <li>Leaving the room in room module</li>
     * </ul>
     *
     * <p>Endpoint: POST /api/lobby/leave
     *
     * @param request  Leave room request containing room code
     * @param playerId Player ID from request header
     * @return CompletionStage with ApiResult containing leave status
     * @author LiYuanXing
     */
    @PostMapping("/leave")
    public CompletionStage<ApiResult<LeaveRoomResponse>> leaveRoom(@RequestBody LeaveRoomRequest request,
                                                                    @RequestHeader("X-User-Id") String playerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Leave room: roomCode={}, playerId={}", request.getRoomCode(), playerId);
            LeaveRoomResponse response = roomBizService.leaveRoom(request, playerId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
