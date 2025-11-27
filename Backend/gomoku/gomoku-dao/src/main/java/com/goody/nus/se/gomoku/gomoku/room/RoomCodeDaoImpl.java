package com.goody.nus.se.gomoku.gomoku.room;

import com.goody.nus.se.gomoku.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Repository
public class RoomCodeDaoImpl implements RoomCodeDao {
    @Autowired
    private RedisService redisService;

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String ROOM_PLAYERS_SUFFIX = ":players";
    private static final String ROOM_STATUS_SUFFIX = ":status";
    @Override
    public void createRoomCode(String roomCode, int ttlMinutes) {
        redisService.set(ROOM_KEY_PREFIX + roomCode + ROOM_STATUS_SUFFIX, "waiting", ttlMinutes, TimeUnit.MINUTES);
        redisService.set(ROOM_KEY_PREFIX + roomCode, "active", ttlMinutes, TimeUnit.MINUTES);
    }

    @Override
    public boolean exists(String roomCode) {
        return redisService.exists(ROOM_KEY_PREFIX + roomCode);
    }

    @Override
    public List<String> getPlayersByRoom(String roomCode) {
        return redisService.lRange(ROOM_KEY_PREFIX + roomCode + ROOM_PLAYERS_SUFFIX, 0, -1);
    }

    @Override
    public void addPlayerToRoom(String roomCode, String playerId) {
        redisService.lPush(ROOM_KEY_PREFIX + roomCode + ROOM_PLAYERS_SUFFIX, playerId);
    }
    
    @Override
    public void updateRoomTTL(String roomCode, int ttlMinutes) {
        redisService.expire(ROOM_KEY_PREFIX + roomCode, ttlMinutes, TimeUnit.MINUTES);
        redisService.expire(ROOM_KEY_PREFIX + roomCode + ROOM_PLAYERS_SUFFIX, ttlMinutes, TimeUnit.MINUTES);
        redisService.expire(ROOM_KEY_PREFIX + roomCode + ROOM_STATUS_SUFFIX, ttlMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void removePlayerFromRoom(String roomCode, String playerId) {
        redisService.getList(ROOM_KEY_PREFIX + roomCode + ROOM_PLAYERS_SUFFIX).remove(playerId);
    }

    @Override
    public void deleteRoom(String roomCode) {
        redisService.delete(ROOM_KEY_PREFIX + roomCode);
        redisService.delete(ROOM_KEY_PREFIX + roomCode + ROOM_PLAYERS_SUFFIX);
        redisService.delete(ROOM_KEY_PREFIX + roomCode + ROOM_STATUS_SUFFIX);
    }

    @Override
    public String findRoomCodeByPlayerId(String playerId) {
        // Search all room:*:players keys for the player
        Iterable<String> keys = redisService.keys(ROOM_KEY_PREFIX + "*" + ROOM_PLAYERS_SUFFIX);
        for (String key : keys) {
            List<String> players = redisService.lRange(key, 0, -1);
            if (players != null && players.contains(playerId)) {
                // Extract room code from key: "room:{roomCode}:players"
                String roomCode = key.substring(ROOM_KEY_PREFIX.length(),
                                                 key.length() - ROOM_PLAYERS_SUFFIX.length());
                return roomCode;
            }
        }
        return null;
    }
}
