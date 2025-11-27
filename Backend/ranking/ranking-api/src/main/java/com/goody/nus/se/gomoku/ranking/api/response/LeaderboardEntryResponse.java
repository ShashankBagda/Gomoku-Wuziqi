package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Leaderboard Entry Response
 * Represents a single player's entry in the leaderboard
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardEntryResponse {

    /**
     * Player user ID
     */
    private Long userId;

    /**
     * Player's current score in this leaderboard
     */
    private Integer score;

    /**
     * Player's rank position (1-based, 1 is the highest)
     */
    private Integer rank;
}
