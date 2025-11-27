package com.goody.nus.se.gomoku.gomoku.biz.service;

import com.goody.nus.se.gomoku.gomoku.api.response.PlayerStatusResponse;

/**
 * Player status business service interface
 * Aggregates player status from matching queue and room modules
 *
 * @author HaoTian
 */
public interface IPlayerStatusService {
    /**
     * Get player's current status across matching and room systems
     *
     * @param playerId the player ID
     * @return player status response containing matching and room status
     */
    PlayerStatusResponse getPlayerStatus(String playerId);
}
