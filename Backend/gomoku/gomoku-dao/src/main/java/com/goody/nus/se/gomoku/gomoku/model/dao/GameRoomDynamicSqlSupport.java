package com.goody.nus.se.gomoku.gomoku.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class GameRoomDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    public static final GameRoom gameRoom = new GameRoom();

    /**
     * Database Column Remarks:
     *   Room ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.id")
    public static final SqlColumn<Long> id = gameRoom.id;

    /**
     * Database Column Remarks:
     *   Room code for joining
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.room_code")
    public static final SqlColumn<String> roomCode = gameRoom.roomCode;

    /**
     * Database Column Remarks:
     *   First player user ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.player1_id")
    public static final SqlColumn<Long> player1Id = gameRoom.player1Id;

    /**
     * Database Column Remarks:
     *   Second player user ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.player2_id")
    public static final SqlColumn<Long> player2Id = gameRoom.player2Id;

    /**
     * Database Column Remarks:
     *   Match type (0=casual, 1=ranked)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.type")
    public static final SqlColumn<Byte> type = gameRoom.type;

    /**
     * Database Column Remarks:
     *   Room status (0=waiting, 1=matched, 2=playing, 3=finished)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.status")
    public static final SqlColumn<Byte> status = gameRoom.status;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.created_at")
    public static final SqlColumn<LocalDateTime> createdAt = gameRoom.createdAt;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: game_room.updated_at")
    public static final SqlColumn<LocalDateTime> updatedAt = gameRoom.updatedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    public static final class GameRoom extends AliasableSqlTable<GameRoom> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<String> roomCode = column("room_code", JDBCType.VARCHAR);

        public final SqlColumn<Long> player1Id = column("player1_id", JDBCType.BIGINT);

        public final SqlColumn<Long> player2Id = column("player2_id", JDBCType.BIGINT);

        public final SqlColumn<Byte> type = column("`type`", JDBCType.TINYINT);

        public final SqlColumn<Byte> status = column("`status`", JDBCType.TINYINT);

        public final SqlColumn<LocalDateTime> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedAt = column("updated_at", JDBCType.TIMESTAMP);

        public GameRoom() {
            super("game_room", GameRoom::new);
        }
    }
}