package com.goody.nus.se.gomoku.gomoku.room;

import java.util.List;

public interface RoomCodeDao {
     void createRoomCode(String roomCode, int ttlMinutes);
     boolean exists(String roomCode);
     List<String> getPlayersByRoom(String roomCode);
     void addPlayerToRoom(String roomCode, String playerId);
     void updateRoomTTL(String roomCode, int ttlMinutes);
     void removePlayerFromRoom(String roomCode, String playerId);
     void deleteRoom(String roomCode);

     /**
      * Find the room code that contains the specified player
      *
      * @param playerId the player ID to search for
      * @return room code if found, null otherwise
      */
     String findRoomCodeByPlayerId(String playerId);
}
