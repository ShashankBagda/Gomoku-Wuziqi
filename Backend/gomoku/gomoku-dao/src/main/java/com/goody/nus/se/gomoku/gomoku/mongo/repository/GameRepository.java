package com.goody.nus.se.gomoku.gomoku.mongo.repository;

import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for game documents
 */
@Repository
public interface GameRepository extends MongoRepository<GameDocument, Long> {

    /**
     * Find game by room ID
     */
    Optional<GameDocument> findByRoomId(Long roomId);
}
