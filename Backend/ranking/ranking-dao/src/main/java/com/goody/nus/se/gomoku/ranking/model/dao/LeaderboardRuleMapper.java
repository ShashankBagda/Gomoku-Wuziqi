package com.goody.nus.se.gomoku.ranking.model.dao;

import static com.goody.nus.se.gomoku.ranking.model.dao.LeaderboardRuleDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.ranking.model.entity.LeaderboardRule;
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
public interface LeaderboardRuleMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<LeaderboardRule>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    BasicColumn[] selectList = BasicColumn.columnList(id, startTime, endTime, type, ruleId, description, createdTime, updatedTime);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="LeaderboardRuleResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="start_time", property="startTime", jdbcType=JdbcType.INTEGER),
        @Result(column="end_time", property="endTime", jdbcType=JdbcType.INTEGER),
        @Result(column="type", property="type", jdbcType=JdbcType.CHAR),
        @Result(column="rule_id", property="ruleId", jdbcType=JdbcType.BIGINT),
        @Result(column="description", property="description", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<LeaderboardRule> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("LeaderboardRuleResult")
    Optional<LeaderboardRule> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int insert(LeaderboardRule row) {
        return MyBatis3Utils.insert(this::insert, row, leaderboardRule, c ->
            c.map(id).toProperty("id")
            .map(startTime).toProperty("startTime")
            .map(endTime).toProperty("endTime")
            .map(type).toProperty("type")
            .map(ruleId).toProperty("ruleId")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int insertMultiple(Collection<LeaderboardRule> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, leaderboardRule, c ->
            c.map(id).toProperty("id")
            .map(startTime).toProperty("startTime")
            .map(endTime).toProperty("endTime")
            .map(type).toProperty("type")
            .map(ruleId).toProperty("ruleId")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int insertSelective(LeaderboardRule row) {
        return MyBatis3Utils.insert(this::insert, row, leaderboardRule, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(startTime).toPropertyWhenPresent("startTime", row::getStartTime)
            .map(endTime).toPropertyWhenPresent("endTime", row::getEndTime)
            .map(type).toPropertyWhenPresent("type", row::getType)
            .map(ruleId).toPropertyWhenPresent("ruleId", row::getRuleId)
            .map(description).toPropertyWhenPresent("description", row::getDescription)
            .map(createdTime).toPropertyWhenPresent("createdTime", row::getCreatedTime)
            .map(updatedTime).toPropertyWhenPresent("updatedTime", row::getUpdatedTime)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default Optional<LeaderboardRule> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default List<LeaderboardRule> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default List<LeaderboardRule> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default Optional<LeaderboardRule> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, leaderboardRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    static UpdateDSL<UpdateModel> updateAllColumns(LeaderboardRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(startTime).equalTo(row::getStartTime)
                .set(endTime).equalTo(row::getEndTime)
                .set(type).equalTo(row::getType)
                .set(ruleId).equalTo(row::getRuleId)
                .set(description).equalTo(row::getDescription)
                .set(createdTime).equalTo(row::getCreatedTime)
                .set(updatedTime).equalTo(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(LeaderboardRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(startTime).equalToWhenPresent(row::getStartTime)
                .set(endTime).equalToWhenPresent(row::getEndTime)
                .set(type).equalToWhenPresent(row::getType)
                .set(ruleId).equalToWhenPresent(row::getRuleId)
                .set(description).equalToWhenPresent(row::getDescription)
                .set(createdTime).equalToWhenPresent(row::getCreatedTime)
                .set(updatedTime).equalToWhenPresent(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int updateByPrimaryKey(LeaderboardRule row) {
        return update(c ->
            c.set(startTime).equalTo(row::getStartTime)
            .set(endTime).equalTo(row::getEndTime)
            .set(type).equalTo(row::getType)
            .set(ruleId).equalTo(row::getRuleId)
            .set(description).equalTo(row::getDescription)
            .set(createdTime).equalTo(row::getCreatedTime)
            .set(updatedTime).equalTo(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    default int updateByPrimaryKeySelective(LeaderboardRule row) {
        return update(c ->
            c.set(startTime).equalToWhenPresent(row::getStartTime)
            .set(endTime).equalToWhenPresent(row::getEndTime)
            .set(type).equalToWhenPresent(row::getType)
            .set(ruleId).equalToWhenPresent(row::getRuleId)
            .set(description).equalToWhenPresent(row::getDescription)
            .set(createdTime).equalToWhenPresent(row::getCreatedTime)
            .set(updatedTime).equalToWhenPresent(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO leaderboard_rule" +
              " (`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") LeaderboardRule record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO leaderboard_rule" +
            " (`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<LeaderboardRule> records);

    @Insert({"<script>" +
            " INSERT INTO leaderboard_rule" +
              "(`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  start_time = r.start_time, end_time = r.end_time, type = r.type, rule_id = r.rule_id, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") LeaderboardRule record);

    @Insert({"<script>" +
            " INSERT INTO leaderboard_rule" +
            " (`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  start_time = r.start_time, end_time = r.end_time, type = r.type, rule_id = r.rule_id, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<LeaderboardRule> records);

    @Insert({"<script>" +
            " REPLACE INTO leaderboard_rule" +
              " (`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") LeaderboardRule record);

    @Insert({"<script>" +
            " REPLACE INTO leaderboard_rule" +
            " (`id`, `start_time`, `end_time`, `type`, `rule_id`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.startTime}, #{item.endTime}, #{item.type}, #{item.ruleId}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<LeaderboardRule> records);
}