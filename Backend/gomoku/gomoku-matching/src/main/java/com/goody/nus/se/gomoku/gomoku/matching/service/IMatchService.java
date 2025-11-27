package com.goody.nus.se.gomoku.gomoku.matching.service;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;

/**
 * Core match service interface
 *
 * <p>Provides low-level matching operations including:
 * <ul>
 *   <li>Player matching and queue management</li>
 *   <li>Queue status queries</li>
 *   <li>Queue cancellation</li>
 * </ul>
 *
 * @author HaoTian
 * @version 1.0, 2025/10/21
 */
public interface IMatchService {
    /**
     * Handle player match request
     *
     * @param request match request containing mode (casual/ranked)
     * @param userId  player ID
     * @return match response with status and room info
     */
    MatchResponse match(MatchRequest request, String userId);

    /**
     * Find which queue the player is currently in
     *
     * @param playerId the player ID to search for
     * @return match mode ("casual" or "ranked") if found, null otherwise
     */
    String findPlayerQueue(String playerId);

    /**
     * Cancel player's current match queue
     *
     * <p>Removes player from whichever queue they are in (casual/ranked).
     * This operation is idempotent - safe to call even if player is not in any queue.
     *
     * @param playerId player ID
     * @return cancel response with status and queue mode that was canceled
     */
    CancelMatchResponse cancelMatch(String playerId);
}
