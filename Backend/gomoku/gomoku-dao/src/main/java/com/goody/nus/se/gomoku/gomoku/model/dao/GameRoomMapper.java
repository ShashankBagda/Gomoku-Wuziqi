package com.goody.nus.se.gomoku.gomoku.model.dao;

import static com.goody.nus.se.gomoku.gomoku.model.dao.GameRoomDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.gomoku.model.entity.GameRoom;
import jakarta.annotation.Generated;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

@Mapper
public interface GameRoomMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<GameRoom>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    BasicColumn[] selectList = BasicColumn.columnList(id, roomCode, player1Id, player2Id, type, status, createdAt, updatedAt);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="GameRoomResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="room_code", property="roomCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="player1_id", property="player1Id", jdbcType=JdbcType.BIGINT),
        @Result(column="player2_id", property="player2Id", jdbcType=JdbcType.BIGINT),
        @Result(column="type", property="type", jdbcType=JdbcType.TINYINT),
        @Result(column="status", property="status", jdbcType=JdbcType.TINYINT),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_at", property="updatedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<GameRoom> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("GameRoomResult")
    Optional<GameRoom> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int insert(GameRoom row) {
        return MyBatis3Utils.insert(this::insert, row, gameRoom, c ->
            c.map(id).toProperty("id")
            .map(roomCode).toProperty("roomCode")
            .map(player1Id).toProperty("player1Id")
            .map(player2Id).toProperty("player2Id")
            .map(type).toProperty("type")
            .map(status).toProperty("status")
            .map(createdAt).toProperty("createdAt")
            .map(updatedAt).toProperty("updatedAt")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int insertMultiple(Collection<GameRoom> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, gameRoom, c ->
            c.map(id).toProperty("id")
            .map(roomCode).toProperty("roomCode")
            .map(player1Id).toProperty("player1Id")
            .map(player2Id).toProperty("player2Id")
            .map(type).toProperty("type")
            .map(status).toProperty("status")
            .map(createdAt).toProperty("createdAt")
            .map(updatedAt).toProperty("updatedAt")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int insertSelective(GameRoom row) {
        return MyBatis3Utils.insert(this::insert, row, gameRoom, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(roomCode).toPropertyWhenPresent("roomCode", row::getRoomCode)
            .map(player1Id).toPropertyWhenPresent("player1Id", row::getPlayer1Id)
            .map(player2Id).toPropertyWhenPresent("player2Id", row::getPlayer2Id)
            .map(type).toPropertyWhenPresent("type", row::getType)
            .map(status).toPropertyWhenPresent("status", row::getStatus)
            .map(createdAt).toPropertyWhenPresent("createdAt", row::getCreatedAt)
            .map(updatedAt).toPropertyWhenPresent("updatedAt", row::getUpdatedAt)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default Optional<GameRoom> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default List<GameRoom> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default List<GameRoom> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default Optional<GameRoom> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, gameRoom, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    static UpdateDSL<UpdateModel> updateAllColumns(GameRoom row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(roomCode).equalTo(row::getRoomCode)
                .set(player1Id).equalTo(row::getPlayer1Id)
                .set(player2Id).equalTo(row::getPlayer2Id)
                .set(type).equalTo(row::getType)
                .set(status).equalTo(row::getStatus)
                .set(createdAt).equalTo(row::getCreatedAt)
                .set(updatedAt).equalTo(row::getUpdatedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(GameRoom row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(roomCode).equalToWhenPresent(row::getRoomCode)
                .set(player1Id).equalToWhenPresent(row::getPlayer1Id)
                .set(player2Id).equalToWhenPresent(row::getPlayer2Id)
                .set(type).equalToWhenPresent(row::getType)
                .set(status).equalToWhenPresent(row::getStatus)
                .set(createdAt).equalToWhenPresent(row::getCreatedAt)
                .set(updatedAt).equalToWhenPresent(row::getUpdatedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int updateByPrimaryKey(GameRoom row) {
        return update(c ->
            c.set(roomCode).equalTo(row::getRoomCode)
            .set(player1Id).equalTo(row::getPlayer1Id)
            .set(player2Id).equalTo(row::getPlayer2Id)
            .set(type).equalTo(row::getType)
            .set(status).equalTo(row::getStatus)
            .set(createdAt).equalTo(row::getCreatedAt)
            .set(updatedAt).equalTo(row::getUpdatedAt)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: game_room")
    default int updateByPrimaryKeySelective(GameRoom row) {
        return update(c ->
            c.set(roomCode).equalToWhenPresent(row::getRoomCode)
            .set(player1Id).equalToWhenPresent(row::getPlayer1Id)
            .set(player2Id).equalToWhenPresent(row::getPlayer2Id)
            .set(type).equalToWhenPresent(row::getType)
            .set(status).equalToWhenPresent(row::getStatus)
            .set(createdAt).equalToWhenPresent(row::getCreatedAt)
            .set(updatedAt).equalToWhenPresent(row::getUpdatedAt)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO game_room" +
              " (`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " VALUES" +
              "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") GameRoom record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO game_room" +
            " (`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<GameRoom> records);

    @Insert({"<script>" +
            " INSERT INTO game_room" +
              "(`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " VALUES" +
              "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  room_code = r.room_code, player1_id = r.player1_id, player2_id = r.player2_id, type = r.type, status = r.status, created_at = r.created_at, updated_at = r.updated_at" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") GameRoom record);

    @Insert({"<script>" +
            " INSERT INTO game_room" +
            " (`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  room_code = r.room_code, player1_id = r.player1_id, player2_id = r.player2_id, type = r.type, status = r.status, created_at = r.created_at, updated_at = r.updated_at" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<GameRoom> records);

    @Insert({"<script>" +
            " REPLACE INTO game_room" +
              " (`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " VALUES" +
              "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") GameRoom record);

    @Insert({"<script>" +
            " REPLACE INTO game_room" +
            " (`id`, `room_code`, `player1_id`, `player2_id`, `type`, `status`, `created_at`, `updated_at`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.roomCode}, #{item.player1Id}, #{item.player2Id}, #{item.type}, #{item.status}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<GameRoom> records);
}