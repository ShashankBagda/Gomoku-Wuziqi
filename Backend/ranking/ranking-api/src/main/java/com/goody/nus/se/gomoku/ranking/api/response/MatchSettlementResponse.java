package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for match settlement operation
 * Contains detailed information about changes for both players
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchSettlementResponse {

    /**
     * Match ID that was settled
     */
    private Long matchId;

    /**
     * Game mode type (RANKED, CASUAL, PRIVATE)
     */
    private String modeType;

    /**
     * Winner's user ID (null if draw)
     */
    private Long winnerId;

    /**
     * Loser's user ID (null if draw)
     */
    private Long loserId;

    /**
     * Winner's rewards (null if draw)
     */
    private PlayerReward winnerReward;

    /**
     * Loser's rewards (null if draw)
     */
    private PlayerReward loserReward;

    /**
     * Player 1's rewards (for draw case)
     */
    private PlayerReward player1Reward;

    /**
     * Player 2's rewards (for draw case)
     */
    private PlayerReward player2Reward;

    /**
     * Settlement timestamp
     */
    private Long timestamp;

    /**
     * Player reward details after match settlement
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlayerReward {
        /**
         * Player user ID
         */
        private Long userId;

        /**
         * Experience gained/lost
         */
        private Integer expChange;

        /**
         * Total experience after settlement
         */
        private Integer totalExp;

        /**
         * Score change (only for RANKED matches)
         */
        private Integer scoreChange;

        /**
         * Total score after settlement
         */
        private Integer totalScore;

        /**
         * Level before settlement
         */
        private Integer oldLevel;

        /**
         * Level after settlement
         */
        private Integer newLevel;

        /**
         * Whether player leveled up
         */
        private Boolean leveledUp;
    }
}
