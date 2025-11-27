package com.goody.nus.se.gomoku.gomoku.game.chain.execute;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * execute chain abstract class
 * Provides room status transition functionality
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Slf4j
public abstract class ExecuteChain {

    @Autowired
    private IGameRoomService gameRoomService;

    /**
     * Check if this chain should execute
     *
     * @param game   the game document
     * @param action the action to execute
     * @return true if this chain should execute
     */
    public abstract boolean check(GameDocument game, GameAction action);

    /**
     * Execute the action
     *
     * @param game   the game document
     * @param action the action to execute
     */
    public abstract void execute(GameDocument game, GameAction action);

    /**
     * Get the action types this chain handles
     *
     * @return the action types
     */
    public abstract List<ActionType> getActionTypes();

    /**
     * Get the sort order
     *
     * @return the sort order
     */
    public abstract int sort();

    /**
     * Update room status in database
     *
     * @param roomId the room ID
     * @param status the new status
     */
    protected void updateRoomStatus(Long roomId, RoomStatusEnum status) {
        gameRoomService.update(GameRoomDTO.builder()
                .id(roomId)
                .status(status.getValue())
                .build());
    }
}
