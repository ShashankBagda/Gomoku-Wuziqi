package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing all ranking rules configuration
 * Allows frontend to calculate expected rewards before settlement
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingRulesResponse {

    /**
     * Experience rules for different modes and results
     */
    private List<ExpRule> expRules;

    /**
     * Score rules for ranked matches
     */
    private List<ScoreRule> scoreRules;

    /**
     * Level experience requirements
     */
    private List<LevelThreshold> levelThresholds;

    /**
     * Active leaderboard configurations
     */
    private List<LeaderboardConfig> leaderboards;

    /**
     * Experience rule
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExpRule {
        private String modeType;      // RANKED, CASUAL, PRIVATE
        private String matchResult;   // WIN, LOSE, DRAW
        private Integer expValue;     // Experience change
        private String description;
    }

    /**
     * Score rule
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ScoreRule {
        private String modeType;      // RANKED, CASUAL, PRIVATE
        private String matchResult;   // WIN, LOSE, DRAW
        private Integer scoreValue;   // Score change
        private String description;
    }

    /**
     * Level threshold
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LevelThreshold {
        private Integer level;
        private Integer expRequired;  // Total exp required to reach this level
    }

    /**
     * Leaderboard configuration
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LeaderboardConfig {
        private String type;          // DAILY, MONTHLY, SEASONAL, TOTAL
        private Long startTime;       // Unix timestamp
        private Long endTime;         // Unix timestamp
        private String description;
    }
}
