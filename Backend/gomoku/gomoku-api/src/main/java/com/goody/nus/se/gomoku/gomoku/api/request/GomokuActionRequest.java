package com.goody.nus.se.gomoku.gomoku.api.request;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.model.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Request for game action
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GomokuActionRequest {

    /**
     * Action type
     */
    @NotNull(message = "Action type cannot be null")
    private ActionType type;

    /**
     * Position (required only for MOVE action)
     */
    private Position position;
}
