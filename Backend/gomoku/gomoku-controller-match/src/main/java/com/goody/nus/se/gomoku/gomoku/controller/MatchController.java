package com.goody.nus.se.gomoku.gomoku.controller;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchBizService;
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
 * Match controller for player matchmaking
 *
 * <p>Provides REST endpoints for:
 * <ul>
 *   <li>Joining match queue (casual/ranked)</li>
 *   <li>Canceling match queue</li>
 * </ul>
 *
 * <p>All operations are executed asynchronously using business thread pool.
 *
 * @author LiYuanXing, Haotian
 * @version 1.0, 2025/10/21
 */
@Slf4j
@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private IMatchBizService matchBizService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Match the player in specified mode
     *
     * <p>Endpoint: POST /api/match
     *
     * <p>Player can only be in one queue at a time. If already in a queue or room,
     * an error will be returned.
     *
     * @param request match request containing mode (casual/ranked)
     * @param userId  player ID from request header
     * @return CompletionStage with match result (waiting/matched/error)
     * @author LiYuanXing
     */
    @PostMapping
    public CompletionStage<ApiResult<MatchResponse>> match(@RequestBody MatchRequest request,
                                                           @RequestHeader("X-User-Id") String userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Match request: mode={}, userId={}", request.getMode(), userId);
            final MatchResponse response = matchBizService.matchAndSave(request, userId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Cancel player's current match queue
     *
     * <p>Endpoint: POST /api/match/cancel
     *
     * <p>Removes player from whichever queue they are currently in.
     * If not in any queue, returns "not_in_queue" status.
     *
     * @param userId player ID from request header
     * @return CompletionStage with cancellation result
     * @author Haotian
     */
    @PostMapping("/cancel")
    public CompletionStage<ApiResult<CancelMatchResponse>> cancelMatch(@RequestHeader("X-User-Id") String userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Cancel match request: userId={}", userId);
            final CancelMatchResponse response = matchBizService.cancelMatch(userId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
