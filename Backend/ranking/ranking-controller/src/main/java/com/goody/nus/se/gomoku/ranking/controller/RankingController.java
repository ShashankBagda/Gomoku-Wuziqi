package com.goody.nus.se.gomoku.ranking.controller;

import com.goody.nus.se.gomoku.ranking.api.request.MatchSettlementRequest;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardsResponse;
import com.goody.nus.se.gomoku.ranking.api.response.MatchSettlementResponse;
import com.goody.nus.se.gomoku.ranking.api.response.PlayerProfileResponse;
import com.goody.nus.se.gomoku.ranking.api.response.RankingRulesResponse;
import com.goody.nus.se.gomoku.ranking.biz.RankingService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Ranking Controller
 * Provides REST API endpoints for ranking and match settlement
 *
 * @author chengmuqin
 * @version 1.2, 2025/10/21
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Get comprehensive player profile
     *
     * <p>Endpoint: GET /ranking/profile
     *
     * <p>Returns current player's comprehensive information including:
     * <ul>
     *   <li>Basic info: level, exp, win rate, total games</li>
     *   <li>Progress: exp to next level, progress percentage</li>
     *   <li>Scores: current scores across all leaderboards (DAILY, WEEKLY, MONTHLY, SEASONAL, TOTAL)</li>
     *   <li>Ranks: current ranks in all active leaderboards with position and total players</li>
     * </ul>
     *
     * @param userId Current user ID from gateway authentication (header)
     * @return CompletionStage with ApiResult containing comprehensive player profile
     */
    @GetMapping("/profile")
    public CompletionStage<ApiResult<PlayerProfileResponse>> getPlayerProfile(@RequestHeader("X-User-Id") Long userId) {
        return CompletableFuture.supplyAsync(() ->

        {
            log.info("Get player profile: userId={}", userId);
            PlayerProfileResponse response = rankingService.getPlayerProfile(userId);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Settle a completed match
     *
     * <p>Endpoint: POST /ranking/settle
     *
     * <p>Processes match results and updates player rankings, scores, and experience.
     * Supports different game modes: RANKED, CASUAL, PRIVATE.
     *
     * <p>Request body should contain:
     * <ul>
     *   <li>matchId - The game/match ID</li>
     *   <li>winnerId - Winner's user ID (null if draw)</li>
     *   <li>loserId - Loser's user ID (null if draw)</li>
     *   <li>modeType - Game mode: RANKED, CASUAL, or PRIVATE</li>
     * </ul>
     *
     * @param request Match settlement request containing match details
     * @return CompletionStage with ApiResult containing settlement confirmation with player rewards
     */
    @PostMapping("/settle")
    public CompletionStage<ApiResult<MatchSettlementResponse>> settleMatch(@Valid @RequestBody MatchSettlementRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Settle match: matchId={}, winnerId={}, loserId={}, modeType={}",
                    request.getMatchId(), request.getWinnerId(),
                    request.getLoserId(), request.getModeType());

            MatchSettlementResponse response = rankingService.settleMatch(request);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Get ranking rules configuration
     *
     * <p>Endpoint: GET /ranking/rules
     *
     * <p>Returns all ranking rules including:
     * <ul>
     *   <li>Experience rules: exp rewards for different game modes and results</li>
     *   <li>Score rules: score changes for ranked matches</li>
     *   <li>Level thresholds: exp required for each level</li>
     *   <li>Leaderboard configs: active leaderboard periods and types</li>
     * </ul>
     *
     * <p>Frontend can use this data to:
     * - Display expected rewards before match ends
     * - Show "You will gain +50 exp, +25 score if you win"
     * - Calculate progress bars and level-up predictions
     *
     * @return CompletionStage with ApiResult containing complete ranking rules
     */
    @GetMapping("/rules")
    public CompletionStage<ApiResult<RankingRulesResponse>> getRankingRules() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Get ranking rules");
            RankingRulesResponse response = rankingService.getRankingRules();
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Get top leaderboards (public endpoint)
     *
     * <p>Endpoint: GET /ranking/leaderboard
     *
     * <p>Returns top 50 players from all three primary leaderboards:
     * <ul>
     *   <li>DAILY leaderboard - top 50 players</li>
     *   <li>WEEKLY leaderboard - top 50 players</li>
     *   <li>MONTHLY leaderboard - top 50 players</li>
     * </ul>
     *
     * <p>This is a public endpoint accessible to all users.
     * User-specific rank information is available in the /profile endpoint.
     *
     * @return CompletionStage with ApiResult containing all three leaderboards with top 50 each
     */
    @GetMapping("/leaderboard")
    public CompletionStage<ApiResult<LeaderboardsResponse>> getLeaderboard() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Get top leaderboards (public)");
            LeaderboardsResponse response = rankingService.getTopLeaderboards(null);
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
