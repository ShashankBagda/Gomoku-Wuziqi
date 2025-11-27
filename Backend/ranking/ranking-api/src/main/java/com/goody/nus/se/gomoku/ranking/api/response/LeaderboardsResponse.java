package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Multiple Leaderboards Response
 * Contains top 50 players from Daily, Weekly, and Monthly leaderboards
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardsResponse {

    /**
     * Daily leaderboard top 50
     */
    private LeaderboardPageResponse daily;

    /**
     * Weekly leaderboard top 50
     */
    private LeaderboardPageResponse weekly;

    /**
     * Monthly leaderboard top 50
     */
    private LeaderboardPageResponse monthly;
}
