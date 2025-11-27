package com.goody.nus.se.gomoku.gomoku.game.service;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.GomokuActionRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.GameStateResponse;

/**
 * Game service interface
 */
public interface IGameService {

    /**
     * Execute a game action and return the updated game state
     *
     * <p>Processing flow:
     * <ol>
     *   <li>Load game document from MongoDB by roomId</li>
     *   <li>Validate player belongs to the game</li>
     *   <li>Build GameAction with player color and timestamp</li>
     *   <li>Execute appropriate handler chain based on action type</li>
     *   <li>Validate chain execution result</li>
     *   <li>Update game document with new state and action</li>
     *   <li>Save to MongoDB</li>
     *   <li>Return updated state to frontend</li>
     * </ol>
     *
     * @param roomId   Room ID
     * @param playerId Player ID (from URL parameter, authenticated)
     * @param request  Action request containing action type and optional position
     * @return GameStateResponse with current state, last action, and ready status
     * @throws BizException if game not found, player not in game, or action invalid
     */
    GameStateResponse executeAction(Long roomId, Long playerId, GomokuActionRequest request);

    /**
     * Query current game state without modification
     *
     * <p>Used by frontend polling to fetch latest game state.
     * This is a read-only operation that validates player access.
     *
     * @param roomId   Room id
     * @param playerId Player ID (for access validation)
     * @return GameStateResponse with current state, last action, and ready status
     * @throws BizException if game not found or player not in game
     */
    GameStateResponse getState(Long roomId, Long playerId);
}
