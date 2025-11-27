package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The response of create room
 * @version v.1
 * @author Li YuanXing
 */
@Data
@AllArgsConstructor
public class CreateRoomResponse {
    String roomCode;
}
