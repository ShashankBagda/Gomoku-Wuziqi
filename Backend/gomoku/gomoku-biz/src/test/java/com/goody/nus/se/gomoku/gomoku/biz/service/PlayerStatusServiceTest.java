package com.goody.nus.se.gomoku.gomoku.biz.service;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.PlayerStatusResponse;
import com.goody.nus.se.gomoku.gomoku.game.TestApplication;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for PlayerStatusService
 *
 * @author HaoTian
 */
@SpringBootTest(classes = TestApplication.class)
public class PlayerStatusServiceTest {

    @Autowired
    private IPlayerStatusService playerStatusService;

    @Autowired
    private IMatchService matchService;

    @Autowired
    private RoomCodeDao roomCodeDao;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void clearRedis() {
        // Clear Redis before each test
        for (String key : redisService.keys("match:*")) {
            redisService.delete(key);
        }
        for (String key : redisService.keys("room:*")) {
            redisService.delete(key);
        }
    }

    /**
     * Test player not in queue and not in room
     */
    @Test
    void testPlayerNotInQueueAndNotInRoom() {
        PlayerStatusResponse response = playerStatusService.getPlayerStatus("player1");

        assertThat(response).isNotNull();
        assertThat(response.getMatchingStatus()).isNotNull();
        assertThat(response.getMatchingStatus().getInQueue()).isFalse();
        assertThat(response.getMatchingStatus().getMode()).isNull();

        assertThat(response.getRoomStatus()).isNotNull();
        assertThat(response.getRoomStatus().getInRoom()).isFalse();
        assertThat(response.getRoomStatus().getRoomCode()).isNull();
        assertThat(response.getRoomStatus().getRoomId()).isNull();
        assertThat(response.getRoomStatus().getStatus()).isNull();
    }

    /**
     * Test player in casual matching queue
     */
    @Test
    void testPlayerInCasualQueue() {
        // Put player in casual queue
        MatchRequest request = new MatchRequest();
        request.setMode("casual");
        matchService.match(request, "player1");

        PlayerStatusResponse response = playerStatusService.getPlayerStatus("player1");

        assertThat(response.getMatchingStatus().getInQueue()).isTrue();
        assertThat(response.getMatchingStatus().getMode()).isEqualTo("casual");
        assertThat(response.getRoomStatus().getInRoom()).isFalse();
    }

    /**
     * Test player in ranked matching queue
     */
    @Test
    void testPlayerInRankedQueue() {
        // Put player in ranked queue
        MatchRequest request = new MatchRequest();
        request.setMode("ranked");
        matchService.match(request, "player2");

        PlayerStatusResponse response = playerStatusService.getPlayerStatus("player2");

        assertThat(response.getMatchingStatus().getInQueue()).isTrue();
        assertThat(response.getMatchingStatus().getMode()).isEqualTo("ranked");
        assertThat(response.getRoomStatus().getInRoom()).isFalse();
    }

    /**
     * Test player in waiting room (alone, waiting for second player)
     */
    @Test
    void testPlayerInWaitingRoom() {
        // Create a room and add one player
        String roomCode = "TEST01";
        roomCodeDao.createRoomCode(roomCode, 3);
        roomCodeDao.addPlayerToRoom(roomCode, "player1");

        PlayerStatusResponse response = playerStatusService.getPlayerStatus("player1");

        assertThat(response.getMatchingStatus().getInQueue()).isFalse();
        assertThat(response.getRoomStatus().getInRoom()).isTrue();
        assertThat(response.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(response.getRoomStatus().getStatus()).isEqualTo("waiting");
        assertThat(response.getRoomStatus().getRoomId()).isNull(); // No roomId until matched
    }

    /**
     * Test player in matched room (two players ready)
     */
    @Test
    void testPlayerInMatchedRoom() {
        // Create a room and add two players
        String roomCode = "TEST02";
        roomCodeDao.createRoomCode(roomCode, 3);
        roomCodeDao.addPlayerToRoom(roomCode, "player1");
        roomCodeDao.addPlayerToRoom(roomCode, "player2");

        // Check player1's status
        PlayerStatusResponse response1 = playerStatusService.getPlayerStatus("player1");

        assertThat(response1.getMatchingStatus().getInQueue()).isFalse();
        assertThat(response1.getRoomStatus().getInRoom()).isTrue();
        assertThat(response1.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(response1.getRoomStatus().getStatus()).isEqualTo("matched");

        // Check player2's status
        PlayerStatusResponse response2 = playerStatusService.getPlayerStatus("player2");

        assertThat(response2.getMatchingStatus().getInQueue()).isFalse();
        assertThat(response2.getRoomStatus().getInRoom()).isTrue();
        assertThat(response2.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(response2.getRoomStatus().getStatus()).isEqualTo("matched");
    }

    /**
     * Test player matched via matching service
     * This simulates the full matching flow
     */
    @Test
    void testPlayerStatusAfterMatching() {
        // Player A joins queue
        MatchRequest requestA = new MatchRequest();
        requestA.setMode("casual");
        matchService.match(requestA, "playerA");

        // Check player A is in queue
        PlayerStatusResponse statusA1 = playerStatusService.getPlayerStatus("playerA");
        assertThat(statusA1.getMatchingStatus().getInQueue()).isTrue();
        assertThat(statusA1.getMatchingStatus().getMode()).isEqualTo("casual");

        // Player B joins and gets matched
        MatchRequest requestB = new MatchRequest();
        requestB.setMode("casual");
        MatchResponse matchResponse = matchService.match(requestB, "playerB");

        assertThat(matchResponse.getStatus()).isEqualTo("matched");
        String roomCode = matchResponse.getRoomCode();

        // Check player A is now in room, not in queue
        PlayerStatusResponse statusA2 = playerStatusService.getPlayerStatus("playerA");
        assertThat(statusA2.getMatchingStatus().getInQueue()).isFalse();
        assertThat(statusA2.getRoomStatus().getInRoom()).isTrue();
        assertThat(statusA2.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(statusA2.getRoomStatus().getStatus()).isEqualTo("matched");

        // Check player B is also in room
        PlayerStatusResponse statusB = playerStatusService.getPlayerStatus("playerB");
        assertThat(statusB.getMatchingStatus().getInQueue()).isFalse();
        assertThat(statusB.getRoomStatus().getInRoom()).isTrue();
        assertThat(statusB.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(statusB.getRoomStatus().getStatus()).isEqualTo("matched");
    }

    /**
     * Test multiple players with different statuses
     */
    @Test
    void testMultiplePlayersWithDifferentStatuses() {
        // Player 1: in casual queue
        MatchRequest req1 = new MatchRequest();
        req1.setMode("casual");
        matchService.match(req1, "player1");

        // Player 2: in ranked queue
        MatchRequest req2 = new MatchRequest();
        req2.setMode("ranked");
        matchService.match(req2, "player2");

        // Player 3: in a waiting room
        String roomCode = "TEST03";
        roomCodeDao.createRoomCode(roomCode, 3);
        roomCodeDao.addPlayerToRoom(roomCode, "player3");

        // Player 4: not in queue or room
        // (no action needed)

        // Verify each player's status
        PlayerStatusResponse status1 = playerStatusService.getPlayerStatus("player1");
        assertThat(status1.getMatchingStatus().getInQueue()).isTrue();
        assertThat(status1.getMatchingStatus().getMode()).isEqualTo("casual");

        PlayerStatusResponse status2 = playerStatusService.getPlayerStatus("player2");
        assertThat(status2.getMatchingStatus().getInQueue()).isTrue();
        assertThat(status2.getMatchingStatus().getMode()).isEqualTo("ranked");

        PlayerStatusResponse status3 = playerStatusService.getPlayerStatus("player3");
        assertThat(status3.getRoomStatus().getInRoom()).isTrue();
        assertThat(status3.getRoomStatus().getRoomCode()).isEqualTo(roomCode);
        assertThat(status3.getRoomStatus().getStatus()).isEqualTo("waiting");

        PlayerStatusResponse status4 = playerStatusService.getPlayerStatus("player4");
        assertThat(status4.getMatchingStatus().getInQueue()).isFalse();
        assertThat(status4.getRoomStatus().getInRoom()).isFalse();
    }
}
