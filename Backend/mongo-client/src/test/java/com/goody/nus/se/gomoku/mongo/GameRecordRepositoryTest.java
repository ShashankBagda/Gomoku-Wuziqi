package com.goody.nus.se.gomoku.mongo;

import com.goody.nus.se.gomoku.mongo.entity.GameRecord;
import com.goody.nus.se.gomoku.mongo.repository.GameRecordRepository;
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
 * MongoDB GameRecordRepository Test
 * Tests CRUD operations and custom queries for game records
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@Disabled("Requires MongoDB connection")
@SpringBootTest(classes = MongoTestApplication.class)
class GameRecordRepositoryTest {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    private GameRecord testRecord;

    @BeforeEach
    void setUp() {
        // Clear test data
        gameRecordRepository.deleteAll();

        // Create test record
        testRecord = new GameRecord();
        testRecord.setPlayerOneId(1001L);
        testRecord.setPlayerTwoId(1002L);
        testRecord.setWinnerId(1001L);
        testRecord.setGameDuration(1800);
        testRecord.setStartTime(LocalDateTime.now().minusMinutes(30));
        testRecord.setEndTime(LocalDateTime.now());
        testRecord.setBoardSize(15);
        testRecord.setTotalMoves(50);
        testRecord.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSaveGameRecord() {
        GameRecord saved = gameRecordRepository.save(testRecord);

        assertNotNull(saved.getId());
        assertEquals(testRecord.getPlayerOneId(), saved.getPlayerOneId());
        assertEquals(testRecord.getPlayerTwoId(), saved.getPlayerTwoId());
        assertEquals(testRecord.getWinnerId(), saved.getWinnerId());
    }

    @Test
    void testFindById() {
        GameRecord saved = gameRecordRepository.save(testRecord);

        Optional<GameRecord> found = gameRecordRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getPlayerOneId(), found.get().getPlayerOneId());
    }

    @Test
    void testFindByPlayerOneIdOrPlayerTwoId() {
        gameRecordRepository.save(testRecord);

        List<GameRecord> foundByPlayerOne = gameRecordRepository.findByPlayerOneIdOrPlayerTwoId(1001L, 1001L);
        List<GameRecord> foundByPlayerTwo = gameRecordRepository.findByPlayerOneIdOrPlayerTwoId(1002L, 1002L);

        assertFalse(foundByPlayerOne.isEmpty());
        assertFalse(foundByPlayerTwo.isEmpty());
        assertEquals(1, foundByPlayerOne.size());
        assertEquals(1, foundByPlayerTwo.size());
    }

    @Test
    void testFindByWinnerId() {
        gameRecordRepository.save(testRecord);

        List<GameRecord> winnerRecords = gameRecordRepository.findByWinnerId(1001L);

        assertFalse(winnerRecords.isEmpty());
        assertEquals(1, winnerRecords.size());
        assertEquals(1001L, winnerRecords.get(0).getWinnerId());
    }

    @Test
    void testFindByStartTimeBetween() {
        gameRecordRepository.save(testRecord);

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        List<GameRecord> records = gameRecordRepository.findByStartTimeBetween(start, end);

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
    }

    @Test
    void testDeleteGameRecord() {
        GameRecord saved = gameRecordRepository.save(testRecord);

        gameRecordRepository.deleteById(saved.getId());

        Optional<GameRecord> found = gameRecordRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateGameRecord() {
        GameRecord saved = gameRecordRepository.save(testRecord);

        saved.setWinnerId(1002L);
        saved.setTotalMoves(60);
        GameRecord updated = gameRecordRepository.save(saved);

        assertEquals(1002L, updated.getWinnerId());
        assertEquals(60, updated.getTotalMoves());
    }

    @Test
    void testFindAll() {
        gameRecordRepository.save(testRecord);

        // Create another record
        GameRecord record2 = new GameRecord();
        record2.setPlayerOneId(2001L);
        record2.setPlayerTwoId(2002L);
        record2.setWinnerId(2002L);
        record2.setGameDuration(2400);
        record2.setStartTime(LocalDateTime.now().minusMinutes(40));
        record2.setEndTime(LocalDateTime.now());
        record2.setBoardSize(15);
        record2.setTotalMoves(70);
        record2.setCreatedAt(LocalDateTime.now());
        gameRecordRepository.save(record2);

        List<GameRecord> allRecords = gameRecordRepository.findAll();

        assertEquals(2, allRecords.size());
    }
}
