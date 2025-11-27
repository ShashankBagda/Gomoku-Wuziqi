package com.goody.nus.se.gomoku.mongo.repository;

import com.goody.nus.se.gomoku.mongo.entity.GameRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sample MongoDB repository for game records
 * Provides CRUD operations and custom queries
 */
@Repository
public interface GameRecordRepository extends MongoRepository<GameRecord, String> {

    /**
     * Find game records by player ID (either player one or player two)
     */
    List<GameRecord> findByPlayerOneIdOrPlayerTwoId(Long playerOneId, Long playerTwoId);

    /**
     * Find game records by winner ID
     */
    List<GameRecord> findByWinnerId(Long winnerId);

    /**
     * Find game records within a time range
     */
    List<GameRecord> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
