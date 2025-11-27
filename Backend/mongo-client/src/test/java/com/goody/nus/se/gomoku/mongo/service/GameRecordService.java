package com.goody.nus.se.gomoku.mongo.service;

import com.goody.nus.se.gomoku.mongo.entity.GameRecord;
import com.goody.nus.se.gomoku.mongo.repository.GameRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Game Record Service
 * Business logic for game record operations
 *
 * @author Goody
 * @version 1.0, 2025/01/05
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;

    /**
     * Save a game record
     */
    public GameRecord saveGameRecord(GameRecord gameRecord) {
        gameRecord.setCreatedAt(LocalDateTime.now());
        return gameRecordRepository.save(gameRecord);
    }

    /**
     * Find game record by ID
     */
    public Optional<GameRecord> findById(String id) {
        return gameRecordRepository.findById(id);
    }

    /**
     * Find all game records for a player
     */
    public List<GameRecord> findPlayerGames(Long playerId) {
        return gameRecordRepository.findByPlayerOneIdOrPlayerTwoId(playerId, playerId);
    }

    /**
     * Find all wins for a player
     */
    public List<GameRecord> findPlayerWins(Long playerId) {
        return gameRecordRepository.findByWinnerId(playerId);
    }

    /**
     * Find games within a time range
     */
    public List<GameRecord> findGamesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return gameRecordRepository.findByStartTimeBetween(startTime, endTime);
    }

    /**
     * Delete game record
     */
    public void deleteGameRecord(String id) {
        gameRecordRepository.deleteById(id);
    }

    /**
     * Calculate win rate for a player
     */
    public double calculateWinRate(Long playerId) {
        List<GameRecord> allGames = findPlayerGames(playerId);
        if (allGames.isEmpty()) {
            return 0.0;
        }

        long wins = allGames.stream()
                .filter(game -> playerId.equals(game.getWinnerId()))
                .count();

        return (double) wins / allGames.size() * 100;
    }
}
