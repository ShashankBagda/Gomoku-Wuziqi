package com.goody.nus.se.gomoku.mongo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Sample MongoDB entity for game records
 * This demonstrates basic MongoDB document mapping
 */
@Document(collection = "game_records")
@Data
public class GameRecord {

    @Id
    private String id;

    @Field("player_one_id")
    private Long playerOneId;

    @Field("player_two_id")
    private Long playerTwoId;

    @Field("winner_id")
    private Long winnerId;

    @Field("game_duration")
    private Integer gameDuration;

    @Field("start_time")
    private LocalDateTime startTime;

    @Field("end_time")
    private LocalDateTime endTime;

    @Field("board_size")
    private Integer boardSize;

    @Field("total_moves")
    private Integer totalMoves;

    @Field("created_at")
    private LocalDateTime createdAt;
}
