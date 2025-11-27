package com.goody.nus.se.gomoku.ranking.biz;

import com.goody.nus.se.gomoku.ranking.api.request.MatchSettlementRequest;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardPageResponse;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardsResponse;
import com.goody.nus.se.gomoku.ranking.api.response.MatchRecordResponse;
import com.goody.nus.se.gomoku.ranking.api.response.MatchSettlementResponse;
import com.goody.nus.se.gomoku.ranking.api.response.PlayerProfileResponse;
import com.goody.nus.se.gomoku.ranking.api.response.RankingRulesResponse;

import java.util.List;

/**
 * Ranking Business Service Interface
 * Provides core ranking functionalities including match settlement, player profile queries,
 * leaderboard management, and rule configuration
 *
 * @author chengmuqin
 * @version 2.0, 2025/10/21
 */
public interface RankingService {

    /**
     * Settle a completed match
     * <p>
     * Calculates and updates player rankings, scores, and experience based on match results.
     * Supports different game modes (RANKED, CASUAL, PRIVATE) with corresponding reward rules.
     * <p>
     * This method ensures idempotency by checking if a match has already been settled.
     *
     * @param request Match settlement request containing match details and participants
     * @return Settlement response with detailed reward information for both players
     */
    MatchSettlementResponse settleMatch(MatchSettlementRequest request);

    /**
     * Get comprehensive player profile
     * <p>
     * Returns player's complete information including:
     * - Basic info: level, exp, win rate, total games
     * - Progress: exp to next level, progress percentage
     * - Scores: current scores across all leaderboards
     * - Ranks: current ranks in DAILY/MONTHLY/SEASONAL/TOTAL leaderboards
     *
     * @param userId Player user ID
     * @return Player profile response with comprehensive information
     */
    PlayerProfileResponse getPlayerProfile(Long userId);

    /**
     * Get leaderboard with optional user rank
     * <p>
     * Returns top players in specified leaderboard scope (DAILY, MONTHLY, SEASONAL).
     * If userId is provided, also includes the current user's rank information even if
     * they are not in the top rankings.
     *
     * @param scope  Leaderboard type (DAILY, MONTHLY, SEASONAL)
     * @param page   Page number (1-based)
     * @param size   Page size (max 50 recommended)
     * @param userId Current user ID (optional, for including user's own rank)
     * @return Leaderboard page response with top players and optional user rank
     */
    LeaderboardPageResponse getLeaderboard(String scope, int page, int size, Long userId);

    /**
     * Get all ranking rules configuration
     * <p>
     * Returns complete rule configuration including:
     * - Experience rules: exp rewards for different modes and results
     * - Score rules: score changes for ranked matches
     * - Level thresholds: exp required for each level
     * - Leaderboard configs: active leaderboard periods and types
     * <p>
     * Frontend can use this to calculate and display expected rewards before match settlement.
     *
     * @return Ranking rules response with all configuration data
     */
    RankingRulesResponse getRankingRules();

    /**
     * Get player's match history
     * <p>
     * Returns paginated match history for a specific player, optionally filtered by game mode.
     *
     * @param userId   Player user ID
     * @param modeType Game mode type (RANKED, CASUAL, PRIVATE), null for all modes
     * @param page     Page number (1-based)
     * @param size     Page size
     * @return List of match record responses
     */
    List<MatchRecordResponse> getMatchHistory(Long userId, String modeType, int page, int size);

    /**
     * Get top 50 players from all three primary leaderboards
     * <p>
     * Returns top 50 players from:
     * - DAILY leaderboard
     * - WEEKLY leaderboard
     * - MONTHLY leaderboard
     * <p>
     * If userId is provided, includes the current user's rank in each leaderboard.
     *
     * @param userId Current user ID (optional, for including user's own rank)
     * @return Leaderboards response containing all three leaderboards with top 50 each
     */
    LeaderboardsResponse getTopLeaderboards(Long userId);
}
