package com.goody.nus.se.gomoku.mongo;

import com.goody.nus.se.gomoku.mongo.entity.GameRecord;
import com.goody.nus.se.gomoku.mongo.repository.GameRecordRepository;
import com.goody.nus.se.gomoku.mongo.service.GameRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MongoDB GameRecordService Test
 * Tests business logic for game record operations
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@Disabled("Requires MongoDB connection")
@SpringBootTest(classes = MongoTestApplication.class)
class GameRecordServiceTest {

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @BeforeEach
    void setUp() {
        // Clear all test data
        gameRecordRepository.deleteAll();
    }

    @Test
    void testSaveGameRecord() {
        GameRecord record = createTestGameRecord(1001L, 1002L, 1001L);

        GameRecord saved = gameRecordService.saveGameRecord(record);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(1001L, saved.getPlayerOneId());
        assertEquals(1002L, saved.getPlayerTwoId());
        assertEquals(1001L, saved.getWinnerId());
    }

    @Test
    void testFindById() {
        GameRecord saved = gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));

        Optional<GameRecord> found = gameRecordService.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testFindPlayerGames() {
        // Create games for player 1001
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1003L, 1001L, 1003L));
        gameRecordService.saveGameRecord(createTestGameRecord(2001L, 2002L, 2001L));

        List<GameRecord> playerGames = gameRecordService.findPlayerGames(1001L);

        assertEquals(2, playerGames.size());
        assertTrue(playerGames.stream().anyMatch(g ->
                g.getPlayerOneId().equals(1001L) || g.getPlayerTwoId().equals(1001L)));
    }

    @Test
    void testFindPlayerWins() {
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1003L, 1001L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1004L, 1004L));

        List<GameRecord> wins = gameRecordService.findPlayerWins(1001L);

        assertEquals(2, wins.size());
        assertTrue(wins.stream().allMatch(g -> g.getWinnerId().equals(1001L)));
    }

    @Test
    void testFindGamesByTimeRange() {
        LocalDateTime now = LocalDateTime.now();

        GameRecord record1 = createTestGameRecord(1001L, 1002L, 1001L);
        record1.setStartTime(now.minusHours(2));
        gameRecordService.saveGameRecord(record1);

        GameRecord record2 = createTestGameRecord(1003L, 1004L, 1003L);
        record2.setStartTime(now.minusMinutes(30));
        gameRecordService.saveGameRecord(record2);

        List<GameRecord> recentGames = gameRecordService.findGamesByTimeRange(
                now.minusHours(1),
                now.plusHours(1)
        );

        assertEquals(1, recentGames.size());
        assertEquals(record2.getPlayerOneId(), recentGames.get(0).getPlayerOneId());
    }

    @Test
    void testDeleteGameRecord() {
        GameRecord saved = gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));

        gameRecordService.deleteGameRecord(saved.getId());

        Optional<GameRecord> found = gameRecordService.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testCalculateWinRate_withWins() {
        // Player 1001 wins 2 out of 3 games
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1003L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1004L, 1004L));

        double winRate = gameRecordService.calculateWinRate(1001L);

        assertEquals(66.67, winRate, 0.01);
    }

    @Test
    void testCalculateWinRate_noGames() {
        double winRate = gameRecordService.calculateWinRate(9999L);

        assertEquals(0.0, winRate);
    }

    @Test
    void testCalculateWinRate_allLosses() {
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1002L));
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1003L, 1003L));

        double winRate = gameRecordService.calculateWinRate(1001L);

        assertEquals(0.0, winRate);
    }

    @Test
    void testCalculateWinRate_allWins() {
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1002L, 1001L));
        gameRecordService.saveGameRecord(createTestGameRecord(1001L, 1003L, 1001L));

        double winRate = gameRecordService.calculateWinRate(1001L);

        assertEquals(100.0, winRate);
    }

    // Helper method to create test game records
    private GameRecord createTestGameRecord(Long playerOneId, Long playerTwoId, Long winnerId) {
        GameRecord record = new GameRecord();
        record.setPlayerOneId(playerOneId);
        record.setPlayerTwoId(playerTwoId);
        record.setWinnerId(winnerId);
        record.setGameDuration(1800);
        record.setStartTime(LocalDateTime.now().minusMinutes(30));
        record.setEndTime(LocalDateTime.now());
        record.setBoardSize(15);
        record.setTotalMoves(50);
        return record;
    }
}
