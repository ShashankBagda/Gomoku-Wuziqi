package com.goody.nus.se.gomoku.gomoku.controller;

import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.GameStateResponse;
import com.goody.nus.se.gomoku.gomoku.game.service.IGameService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Game controller for Gomoku game operations
 *
 * <p>Provides REST API endpoints for Gomoku game interaction:
 * <ul>
 *   <li>POST /api/game/{roomId}/action - Execute game action (READY, MOVE, SURRENDER)</li>
 *   <li>GET /api/game/{roomId}/state - Query current game state (for polling)</li>
 * </ul>
 *
 * <p>Uses async processing with CompletionStage to handle concurrent requests efficiently.
 * Frontend should poll the state endpoint periodically to get updates.
 *
 * @author Goody
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final IGameService gameService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Execute game action asynchronously
     *
     * <p>Supported action types:
     * <ul>
     *   <li>READY - Player marks ready (when both ready, game auto-starts)</li>
     *   <li>MOVE - Player places stone at position</li>
     *   <li>SURRENDER - Player surrenders (opponent wins)</li>
     * </ul>
     *
     * <p>Endpoint: POST /api/game/{roomId}/action
     *
     * @param roomId   Room ID (path variable)
     * @param playerId Player ID (query parameter, from authentication)
     * @param request  Action request containing type and optional position
     * @return CompletionStage with ApiResult containing updated game state
     */
    @PostMapping("/{roomId}/action")
    public CompletionStage<ApiResult<GameStateResponse>> executeAction(@PathVariable Long roomId,
                                                                       @RequestHeader("X-User-Id") Long playerId,
                                                                       @Valid @RequestBody GomokuActionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Execute action: roomId={}, playerId={}, actionType={}",
                    roomId, playerId, request.getType());
            GameStateResponse response = gameService.executeAction(roomId, playerId, request);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Query current game state asynchronously
     *
     * <p>Used by frontend polling to fetch latest game state.
     * Frontend should call this endpoint periodically (e.g., every 1-2 seconds)
     * to stay updated with game changes.
     *
     * <p>Endpoint: GET /api/game/{roomId}/state
     *
     * @param roomId   Room ID (path variable)
     * @param playerId Player ID (query parameter, for access validation)
     * @return CompletionStage with ApiResult containing current game state
     */
    @GetMapping("/{roomId}/state")
    public CompletionStage<ApiResult<GameStateResponse>> getState(@PathVariable Long roomId,
                                                                  @RequestHeader("X-User-Id") Long playerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Get game state: roomId={}, playerId={}", roomId, playerId);
            GameStateResponse response = gameService.getState(roomId, playerId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
