package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response containing player's ranks across all leaderboards
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerRanksResponse {

    /**
     * Player user ID
     */
    private Long userId;

    /**
     * Ranks in different leaderboards
     * Key: leaderboard type (DAILY, MONTHLY, SEASONAL, TOTAL)
     * Value: rank information
     */
    private Map<String, RankInfo> ranks;

    /**
     * Rank information for a specific leaderboard
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RankInfo {
        /**
         * Leaderboard type
         */
        private String type;

        /**
         * Player's current rank (1-based, null if not ranked yet)
         */
        private Integer rank;

        /**
         * Player's current score in this leaderboard
         */
        private Integer score;

        /**
         * Total number of players in this leaderboard
         */
        private Integer totalPlayers;

        /**
         * Player's experience (only for TOTAL leaderboard)
         */
        private Integer exp;

        /**
         * Player's level (only for TOTAL leaderboard)
         */
        private Integer level;
    }
}
