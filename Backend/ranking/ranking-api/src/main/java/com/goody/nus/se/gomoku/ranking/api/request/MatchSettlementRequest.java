package com.goody.nus.se.gomoku.ranking.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for settling a completed match
 * Simplified version focusing on essential information
 *
 * @author chengmuqin
 * @version 1.0, 2025/10/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchSettlementRequest {

    /**
     * Match/Game ID
     */
    @NotNull(message = "Match ID cannot be null")
    private Long matchId;

    /**
     * Winner user ID (null if draw)
     */
    private Long winnerId;

    /**
     * Loser user ID (null if draw)
     */
    private Long loserId;

    /**
     * Game mode type: RANKED, CASUAL, PRIVATE
     */
    @NotNull(message = "Mode type cannot be null")
    private String modeType;
}
