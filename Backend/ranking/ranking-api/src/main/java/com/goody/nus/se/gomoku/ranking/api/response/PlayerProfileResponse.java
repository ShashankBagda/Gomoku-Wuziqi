package com.goody.nus.se.gomoku.ranking.api.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Player Profile Response
 * Contains comprehensive player information including level, exp, win rate, and ranks across all leaderboards
 *
 * @author chengmuqin
 * @version 2.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerProfileResponse {
    /**
     * Player user ID
     */
    private Long userId;

    /**
     * Current level
     */
    private Integer level;

    /**
     * Total experience points
     */
    private Integer exp;

    /**
     * Win rate percentage (e.g., "75.5%")
     */
    private String winRate;

    /**
     * Total games played
     */
    private Integer totalGames;

    /**
     * Total EXP required for the next level
     */
    private Integer nextLevelExpRequired;

    /**
     * Remaining EXP needed to reach next level
     */
    private Integer expToNext;

    /**
     * Current level progress percentage (0~100)
     */
    private Integer progressPercent;

    /**
     * Current scores for each leaderboard type
     * Key: leaderboard type (DAILY, MONTHLY, SEASONAL, TOTAL)
     * Value: current score
     */
    private Map<String, Integer> scores;

    /**
     * Player's ranks across all leaderboards
     * Key: leaderboard type (DAILY, MONTHLY, SEASONAL, TOTAL)
     * Value: rank information
     */
    private Map<String, LeaderboardRank> ranks;

    /**
     * Rank information for a specific leaderboard
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LeaderboardRank {
        /**
         * Leaderboard type
         */
        private String type;

        /**
         * Player's current rank (1-based, null if not ranked yet)
         */
        private Integer rank;

        /**
         * Total number of players in this leaderboard
         */
        private Integer totalPlayers;

        /**
         * Player's score in this leaderboard
         */
        private Integer score;
    }
}
