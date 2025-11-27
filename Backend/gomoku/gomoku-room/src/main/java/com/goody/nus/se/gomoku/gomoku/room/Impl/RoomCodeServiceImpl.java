package com.goody.nus.se.gomoku.gomoku.room.Impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.LeaveRoomResponse;
import com.goody.nus.se.gomoku.gomoku.enums.RoomStatusEnum;
import com.goody.nus.se.gomoku.gomoku.enums.RoomTypeEnum;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.room.Service.RoomCodeService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IRoomStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class RoomCodeServiceImpl implements RoomCodeService {
    @Autowired
    private RoomCodeDao roomCodeDao;

    @Autowired
    private IGameRoomService gameRoomService;

    @Autowired
    private IRoomStateService roomStateService;

    private static final int ROOM_TTL_AFTER_ONE_LEFT = 3;

    /**
     * create room code service
     * In checking
     *
     * @author LiYuanXing
     */
    @Override
    public String createRoom() {
        int maxTry = 10, ttlMinutes = 3, bound = 1000000;
        String roomCode = String.format("%06d", new Random().nextInt(bound));
        for (int trying = 0; trying < maxTry; trying++) {
            if (!roomCodeDao.exists(roomCode)) {
                roomCodeDao.createRoomCode(roomCode, ttlMinutes);
                return roomCode;
            }
        }
        throw new BizException(ErrorCodeEnum.RETRY, "create room code");
    }

    /**
     * join room service
     *
     * @author LiYuanXing
     */
    @Override
    public JoinRoomResponse joinRoom(JoinRoomRequest request, String playerId) {
        if (!roomCodeDao.exists(request.getRoomCode())) {
            return new JoinRoomResponse("Not Found", null);
        }
        List<String> players = roomCodeDao.getPlayersByRoom(request.getRoomCode());

        // Check if player is already in the room (deduplication for idempotency)
        if (players != null && players.contains(playerId)) {
            // Player is already in the room, return appropriate status
            if (players.size() == 1) {
                return new JoinRoomResponse("waiting", null);
            } else if (players.size() == 2) {
                // Room is already matched, query roomId from database
                Long roomId = gameRoomService.findRoomIdByRoomCode(request.getRoomCode());
                return new JoinRoomResponse(roomId, "matched", players);
            }
        }

        if (players == null || players.isEmpty()) {
            roomCodeDao.addPlayerToRoom(request.getRoomCode(), playerId);
            roomCodeDao.updateRoomTTL(request.getRoomCode(), 3);
            return new JoinRoomResponse("waiting", null);
        }

        if (players.size() == 1) {
            roomCodeDao.addPlayerToRoom(request.getRoomCode(), playerId);
            List<String> updatedPlayers = roomCodeDao.getPlayersByRoom(request.getRoomCode());
            roomCodeDao.updateRoomTTL(request.getRoomCode(), 20);

            // Save room record to database when matched
            Long roomId = null;
            if (updatedPlayers.size() == 2) {
                Long player1Id = Long.parseLong(updatedPlayers.get(0));
                Long player2Id = Long.parseLong(updatedPlayers.get(1));

                // Step 1: Save room record to MySQL (game_room table)
                roomId = saveRoomRecord(
                        request.getRoomCode(),
                        player1Id,
                        player2Id,
                        RoomTypeEnum.PRIVATE.getValue()
                );

                // Step 2: Initialize game state in MongoDB (games collection)
                // This ensures that when clients query game state, the document already exists
                roomStateService.initializeGameState(roomId, player1Id, player2Id, "PRIVATE");
                log.info("[RoomCode] Initialized game state in MongoDB: roomId={}, modeType=PRIVATE", roomId);
            }

            return new JoinRoomResponse(roomId, "matched", updatedPlayers);
        }

        // room is full
        return new JoinRoomResponse("full", players);
    }

    /**
     * leave room service
     *
     * @author LiYuanXing
     */
    @Override
    public LeaveRoomResponse leaveRoom(LeaveRoomRequest request, String playerId) {
        String roomCode = request.getRoomCode();

        LeaveRoomResponse response = new LeaveRoomResponse();

        if (!roomCodeDao.exists(roomCode)) {
            response.setStatus("notFound");
            response.setMessage("Room not found");
            return response;
        }

        roomCodeDao.removePlayerFromRoom(roomCode, playerId);
        List<String> remainingPlayers = roomCodeDao.getPlayersByRoom(roomCode);

        if (remainingPlayers.isEmpty()) {
            roomCodeDao.deleteRoom(roomCode);
            response.setStatus("empty");
            response.setMessage("Room deleted (no players left)");
        } else {
            // 房间还有一名玩家，设置短TTL
            roomCodeDao.updateRoomTTL(roomCode, ROOM_TTL_AFTER_ONE_LEFT);
            response.setStatus("success");
            response.setMessage("Player left; room TTL shortened to 3 minutes");
        }

        return response;
    }

    /**
     * Save room record to database when two players are matched
     *
     * @author LiYuanXing
     */
    @Override
    public Long saveRoomRecord(String roomCode, Long player1Id, Long player2Id, byte roomType) {
        GameRoomDTO gameRoomDTO = GameRoomDTO.builder()
                .roomCode(roomCode)
                .player1Id(player1Id)
                .player2Id(player2Id)
                .type(roomType)
                .status(RoomStatusEnum.MATCHED.getValue())
                .build();
        return gameRoomService.save(gameRoomDTO);
    }
}
