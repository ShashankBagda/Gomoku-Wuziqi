package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The response of join room
 *
 * @author Li YuanXing
 * @version v.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRoomResponse {
    private Long roomId;
    private String status; //waiting;matched;full;not found;
    private List<String> players;

    public JoinRoomResponse(String status, List<String> players) {
        this.status = status;
        this.players = players;
    }
}
