package com.goody.nus.se.gomoku.ranking.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Record Response
 * Represents a single match record in player's match history
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchRecordResponse {

    /**
     * Match ID
     */
    private Long matchId;

    /**
     * Game mode type (RANKED, CASUAL, PRIVATE)
     */
    private String modeType;

    /**
     * Match result for this player (WIN, LOSE, DRAW)
     */
    private String matchResult;

    /**
     * Opponent user ID
     */
    private Long opponentId;

    /**
     * Opponent nickname
     */
    private String opponentNickname;

    /**
     * Opponent avatar URL
     */
    private String opponentAvatarUrl;

    /**
     * Score change in this match (can be negative)
     */
    private Integer scoreChange;

    /**
     * Experience gained in this match
     */
    private Integer expGained;

    /**
     * Match completion timestamp (Unix timestamp in milliseconds)
     */
    private Long timestamp;
}
