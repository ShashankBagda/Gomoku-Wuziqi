package com.goody.nus.se.gomoku.gomoku.controller;

import com.goody.nus.se.gomoku.gomoku.api.response.PlayerStatusResponse;
import com.goody.nus.se.gomoku.gomoku.biz.service.IPlayerStatusService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Player status controller
 * Provides API to check player's current status in matching queue or room
 *
 * @author HaoTian
 */
@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerStatusController {

    private final IPlayerStatusService playerStatusService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Get player's current status asynchronously
     * Used by frontend on app initialization to check if player is already in a queue or room
     *
     * @param playerId Player ID from request header
     * @return CompletionStage with player status containing matching and room information
     * @author LiYuanXing
     */
    @GetMapping("/status")
    public CompletionStage<ApiResult<PlayerStatusResponse>> getPlayerStatus(@RequestHeader("X-User-Id") String playerId) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerStatusResponse response = playerStatusService.getPlayerStatus(playerId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
