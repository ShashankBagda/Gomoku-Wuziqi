package com.goody.nus.se.gomoku.ranking.model.dao;

import static com.goody.nus.se.gomoku.ranking.model.dao.RankingDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.ranking.model.entity.Ranking;
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
public interface RankingMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Ranking>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    BasicColumn[] selectList = BasicColumn.columnList(id, userId, leaderboardRuleId, totalExp, levelId, currentTotalScore, rankPosition, createdTime, updatedTime);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="RankingResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        @Result(column="leaderboard_rule_id", property="leaderboardRuleId", jdbcType=JdbcType.BIGINT),
        @Result(column="total_exp", property="totalExp", jdbcType=JdbcType.INTEGER),
        @Result(column="level_id", property="levelId", jdbcType=JdbcType.BIGINT),
        @Result(column="current_total_score", property="currentTotalScore", jdbcType=JdbcType.INTEGER),
        @Result(column="rank_position", property="rankPosition", jdbcType=JdbcType.INTEGER),
        @Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<Ranking> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("RankingResult")
    Optional<Ranking> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int insert(Ranking row) {
        return MyBatis3Utils.insert(this::insert, row, ranking, c ->
            c.map(id).toProperty("id")
            .map(userId).toProperty("userId")
            .map(leaderboardRuleId).toProperty("leaderboardRuleId")
            .map(totalExp).toProperty("totalExp")
            .map(levelId).toProperty("levelId")
            .map(currentTotalScore).toProperty("currentTotalScore")
            .map(rankPosition).toProperty("rankPosition")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int insertMultiple(Collection<Ranking> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, ranking, c ->
            c.map(id).toProperty("id")
            .map(userId).toProperty("userId")
            .map(leaderboardRuleId).toProperty("leaderboardRuleId")
            .map(totalExp).toProperty("totalExp")
            .map(levelId).toProperty("levelId")
            .map(currentTotalScore).toProperty("currentTotalScore")
            .map(rankPosition).toProperty("rankPosition")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int insertSelective(Ranking row) {
        return MyBatis3Utils.insert(this::insert, row, ranking, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(userId).toPropertyWhenPresent("userId", row::getUserId)
            .map(leaderboardRuleId).toPropertyWhenPresent("leaderboardRuleId", row::getLeaderboardRuleId)
            .map(totalExp).toPropertyWhenPresent("totalExp", row::getTotalExp)
            .map(levelId).toPropertyWhenPresent("levelId", row::getLevelId)
            .map(currentTotalScore).toPropertyWhenPresent("currentTotalScore", row::getCurrentTotalScore)
            .map(rankPosition).toPropertyWhenPresent("rankPosition", row::getRankPosition)
            .map(createdTime).toPropertyWhenPresent("createdTime", row::getCreatedTime)
            .map(updatedTime).toPropertyWhenPresent("updatedTime", row::getUpdatedTime)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default Optional<Ranking> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default List<Ranking> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default List<Ranking> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default Optional<Ranking> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, ranking, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    static UpdateDSL<UpdateModel> updateAllColumns(Ranking row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(userId).equalTo(row::getUserId)
                .set(leaderboardRuleId).equalTo(row::getLeaderboardRuleId)
                .set(totalExp).equalTo(row::getTotalExp)
                .set(levelId).equalTo(row::getLevelId)
                .set(currentTotalScore).equalTo(row::getCurrentTotalScore)
                .set(rankPosition).equalTo(row::getRankPosition)
                .set(createdTime).equalTo(row::getCreatedTime)
                .set(updatedTime).equalTo(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(Ranking row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(userId).equalToWhenPresent(row::getUserId)
                .set(leaderboardRuleId).equalToWhenPresent(row::getLeaderboardRuleId)
                .set(totalExp).equalToWhenPresent(row::getTotalExp)
                .set(levelId).equalToWhenPresent(row::getLevelId)
                .set(currentTotalScore).equalToWhenPresent(row::getCurrentTotalScore)
                .set(rankPosition).equalToWhenPresent(row::getRankPosition)
                .set(createdTime).equalToWhenPresent(row::getCreatedTime)
                .set(updatedTime).equalToWhenPresent(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int updateByPrimaryKey(Ranking row) {
        return update(c ->
            c.set(userId).equalTo(row::getUserId)
            .set(leaderboardRuleId).equalTo(row::getLeaderboardRuleId)
            .set(totalExp).equalTo(row::getTotalExp)
            .set(levelId).equalTo(row::getLevelId)
            .set(currentTotalScore).equalTo(row::getCurrentTotalScore)
            .set(rankPosition).equalTo(row::getRankPosition)
            .set(createdTime).equalTo(row::getCreatedTime)
            .set(updatedTime).equalTo(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    default int updateByPrimaryKeySelective(Ranking row) {
        return update(c ->
            c.set(userId).equalToWhenPresent(row::getUserId)
            .set(leaderboardRuleId).equalToWhenPresent(row::getLeaderboardRuleId)
            .set(totalExp).equalToWhenPresent(row::getTotalExp)
            .set(levelId).equalToWhenPresent(row::getLevelId)
            .set(currentTotalScore).equalToWhenPresent(row::getCurrentTotalScore)
            .set(rankPosition).equalToWhenPresent(row::getRankPosition)
            .set(createdTime).equalToWhenPresent(row::getCreatedTime)
            .set(updatedTime).equalToWhenPresent(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO ranking" +
              " (`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") Ranking record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO ranking" +
            " (`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<Ranking> records);

    @Insert({"<script>" +
            " INSERT INTO ranking" +
              "(`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  user_id = r.user_id, leaderboard_rule_id = r.leaderboard_rule_id, total_exp = r.total_exp, level_id = r.level_id, current_total_score = r.current_total_score, rank_position = r.rank_position, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") Ranking record);

    @Insert({"<script>" +
            " INSERT INTO ranking" +
            " (`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  user_id = r.user_id, leaderboard_rule_id = r.leaderboard_rule_id, total_exp = r.total_exp, level_id = r.level_id, current_total_score = r.current_total_score, rank_position = r.rank_position, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<Ranking> records);

    @Insert({"<script>" +
            " REPLACE INTO ranking" +
              " (`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") Ranking record);

    @Insert({"<script>" +
            " REPLACE INTO ranking" +
            " (`id`, `user_id`, `leaderboard_rule_id`, `total_exp`, `level_id`, `current_total_score`, `rank_position`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.userId}, #{item.leaderboardRuleId}, #{item.totalExp}, #{item.levelId}, #{item.currentTotalScore}, #{item.rankPosition}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<Ranking> records);
}