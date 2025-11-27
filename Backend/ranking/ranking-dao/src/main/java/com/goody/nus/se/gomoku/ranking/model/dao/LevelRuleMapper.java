package com.goody.nus.se.gomoku.ranking.model.dao;

import static com.goody.nus.se.gomoku.ranking.model.dao.LevelRuleDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.ranking.model.entity.LevelRule;
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
public interface LevelRuleMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<LevelRule>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    BasicColumn[] selectList = BasicColumn.columnList(id, modeType, matchResult, expValue, description, createdTime, updatedTime);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="LevelRuleResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="mode_type", property="modeType", jdbcType=JdbcType.CHAR),
        @Result(column="match_result", property="matchResult", jdbcType=JdbcType.CHAR),
        @Result(column="exp_value", property="expValue", jdbcType=JdbcType.INTEGER),
        @Result(column="description", property="description", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<LevelRule> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("LevelRuleResult")
    Optional<LevelRule> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int insert(LevelRule row) {
        return MyBatis3Utils.insert(this::insert, row, levelRule, c ->
            c.map(id).toProperty("id")
            .map(modeType).toProperty("modeType")
            .map(matchResult).toProperty("matchResult")
            .map(expValue).toProperty("expValue")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int insertMultiple(Collection<LevelRule> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, levelRule, c ->
            c.map(id).toProperty("id")
            .map(modeType).toProperty("modeType")
            .map(matchResult).toProperty("matchResult")
            .map(expValue).toProperty("expValue")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int insertSelective(LevelRule row) {
        return MyBatis3Utils.insert(this::insert, row, levelRule, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(modeType).toPropertyWhenPresent("modeType", row::getModeType)
            .map(matchResult).toPropertyWhenPresent("matchResult", row::getMatchResult)
            .map(expValue).toPropertyWhenPresent("expValue", row::getExpValue)
            .map(description).toPropertyWhenPresent("description", row::getDescription)
            .map(createdTime).toPropertyWhenPresent("createdTime", row::getCreatedTime)
            .map(updatedTime).toPropertyWhenPresent("updatedTime", row::getUpdatedTime)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default Optional<LevelRule> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default List<LevelRule> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default List<LevelRule> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default Optional<LevelRule> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, levelRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    static UpdateDSL<UpdateModel> updateAllColumns(LevelRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(modeType).equalTo(row::getModeType)
                .set(matchResult).equalTo(row::getMatchResult)
                .set(expValue).equalTo(row::getExpValue)
                .set(description).equalTo(row::getDescription)
                .set(createdTime).equalTo(row::getCreatedTime)
                .set(updatedTime).equalTo(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(LevelRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(modeType).equalToWhenPresent(row::getModeType)
                .set(matchResult).equalToWhenPresent(row::getMatchResult)
                .set(expValue).equalToWhenPresent(row::getExpValue)
                .set(description).equalToWhenPresent(row::getDescription)
                .set(createdTime).equalToWhenPresent(row::getCreatedTime)
                .set(updatedTime).equalToWhenPresent(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int updateByPrimaryKey(LevelRule row) {
        return update(c ->
            c.set(modeType).equalTo(row::getModeType)
            .set(matchResult).equalTo(row::getMatchResult)
            .set(expValue).equalTo(row::getExpValue)
            .set(description).equalTo(row::getDescription)
            .set(createdTime).equalTo(row::getCreatedTime)
            .set(updatedTime).equalTo(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    default int updateByPrimaryKeySelective(LevelRule row) {
        return update(c ->
            c.set(modeType).equalToWhenPresent(row::getModeType)
            .set(matchResult).equalToWhenPresent(row::getMatchResult)
            .set(expValue).equalToWhenPresent(row::getExpValue)
            .set(description).equalToWhenPresent(row::getDescription)
            .set(createdTime).equalToWhenPresent(row::getCreatedTime)
            .set(updatedTime).equalToWhenPresent(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO level_rule" +
              " (`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") LevelRule record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO level_rule" +
            " (`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<LevelRule> records);

    @Insert({"<script>" +
            " INSERT INTO level_rule" +
              "(`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  mode_type = r.mode_type, match_result = r.match_result, exp_value = r.exp_value, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") LevelRule record);

    @Insert({"<script>" +
            " INSERT INTO level_rule" +
            " (`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  mode_type = r.mode_type, match_result = r.match_result, exp_value = r.exp_value, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<LevelRule> records);

    @Insert({"<script>" +
            " REPLACE INTO level_rule" +
              " (`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") LevelRule record);

    @Insert({"<script>" +
            " REPLACE INTO level_rule" +
            " (`id`, `mode_type`, `match_result`, `exp_value`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.modeType}, #{item.matchResult}, #{item.expValue}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<LevelRule> records);
}