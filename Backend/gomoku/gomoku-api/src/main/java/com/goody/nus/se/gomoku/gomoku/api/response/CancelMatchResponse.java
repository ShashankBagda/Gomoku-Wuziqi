package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for canceling match queue
 *
 * @author Haotian
 * @version 1.0, 2025/10/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelMatchResponse {
    /**
     * Status of cancellation ("success" or "not_in_queue")
     */
    private String status;

    /**
     * Descriptive message about the cancellation result
     */
    private String message;

    /**
     * The queue mode that was canceled (e.g., "casual", "ranked")
     * Null if player was not in any queue
     */
    private String queueMode;
}
