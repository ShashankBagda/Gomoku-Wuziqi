package com.goody.nus.se.gomoku.gomoku.game.chain.validate;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.enums.GameStatus;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;

import java.util.List;

/**
 * validate chain interface
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
public interface ValidateChain {

    /**
     * validate the game status
     *
     * @return the game status
     */
    GameStatus validateStatus();

    /**
     * validate the action
     *
     * @param game   the game document
     * @param action the action to validate
     * @return true if valid, false otherwise
     */
    boolean validate(GameDocument game, GameAction action);

    /**
     * get the action types
     *
     * @return the action types
     */
    List<ActionType> getActionTypes();

    /**
     * get the sort order
     *
     * @return the sort order
     */
    int sort();
}
