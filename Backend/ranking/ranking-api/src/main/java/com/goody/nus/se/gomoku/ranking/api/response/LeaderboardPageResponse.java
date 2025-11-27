package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Leaderboard Page Response
 * Contains leaderboard data including top players and optional current user rank
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardPageResponse {

    /**
     * Leaderboard scope/type (DAILY, MONTHLY, SEASONAL, TOTAL)
     */
    private String scope;

    /**
     * Top players list (typically top 50)
     * Sorted by score descending
     */
    private List<LeaderboardEntryResponse> topList;

    /**
     * Current user's rank entry (optional)
     * Included when userId is provided in request, even if not in top list
     * Null if user has no ranking data yet
     */
    private LeaderboardEntryResponse me;

    /**
     * Total number of players in this leaderboard
     */
    private Integer totalPlayers;
}
