package com.goody.nus.se.gomoku.gomoku.matching.service;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;

/**
 * Business layer service for matching operations
 *
 * <p>Orchestrates matching logic and room persistence across multiple services.
 * Ensures high cohesion and low coupling by delegating core match logic to IMatchService.
 *
 * @author Haotian
 * @version 1.0, 2025/10/21
 */
public interface IMatchBizService {
    /**
     * Match player and save room record if matched successfully
     *
     * <p>This method coordinates:
     * <ul>
     *   <li>Player matching via match service</li>
     *   <li>Room record creation in MySQL</li>
     *   <li>Game state initialization in MongoDB</li>
     * </ul>
     *
     * @param request  match request containing mode (casual/ranked)
     * @param playerId player ID
     * @return match response with room info and status
     */
    MatchResponse matchAndSave(MatchRequest request, String playerId);

    /**
     * Cancel player's current match queue
     *
     * <p>Removes player from whichever queue they are in (casual/ranked).
     * This is a simple pass-through to the match service layer.
     *
     * @param playerId player ID
     * @return cancel response with status and queue mode
     */
    CancelMatchResponse cancelMatch(String playerId);
}
