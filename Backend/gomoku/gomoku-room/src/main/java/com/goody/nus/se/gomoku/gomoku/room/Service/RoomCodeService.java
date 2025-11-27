package com.goody.nus.se.gomoku.gomoku.room.Service;

import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;

public interface RoomCodeService {
    String createRoom();

    JoinRoomResponse joinRoom(JoinRoomRequest request, String playerId);

    LeaveRoomResponse leaveRoom(LeaveRoomRequest request, String playerId);

    /**
     * Save room record to database when two players are matched
     *
     * @param roomCode room code
     * @param player1Id first player ID
     * @param player2Id second player ID
     * @param roomType room type (0=casual, 1=ranked, 2=private)
     */
    Long saveRoomRecord(String roomCode, Long player1Id, Long player2Id, byte roomType);
}
