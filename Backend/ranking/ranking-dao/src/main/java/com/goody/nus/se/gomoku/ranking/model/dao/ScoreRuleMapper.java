package com.goody.nus.se.gomoku.ranking.model.dao;

import static com.goody.nus.se.gomoku.ranking.model.dao.ScoreRuleDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.ranking.model.entity.ScoreRule;
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
public interface ScoreRuleMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<ScoreRule>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    BasicColumn[] selectList = BasicColumn.columnList(id, ruleName, rankedWinScore, rankedLoseScore, rankedDrawScore, casualWinScore, casualLoseScore, casualDrawScore, privateWinScore, privateLoseScore, privateDrawScore, description, createdTime, updatedTime);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ScoreRuleResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="rule_name", property="ruleName", jdbcType=JdbcType.VARCHAR),
        @Result(column="ranked_win_score", property="rankedWinScore", jdbcType=JdbcType.INTEGER),
        @Result(column="ranked_lose_score", property="rankedLoseScore", jdbcType=JdbcType.INTEGER),
        @Result(column="ranked_draw_score", property="rankedDrawScore", jdbcType=JdbcType.INTEGER),
        @Result(column="casual_win_score", property="casualWinScore", jdbcType=JdbcType.INTEGER),
        @Result(column="casual_lose_score", property="casualLoseScore", jdbcType=JdbcType.INTEGER),
        @Result(column="casual_draw_score", property="casualDrawScore", jdbcType=JdbcType.INTEGER),
        @Result(column="private_win_score", property="privateWinScore", jdbcType=JdbcType.INTEGER),
        @Result(column="private_lose_score", property="privateLoseScore", jdbcType=JdbcType.INTEGER),
        @Result(column="private_draw_score", property="privateDrawScore", jdbcType=JdbcType.INTEGER),
        @Result(column="description", property="description", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<ScoreRule> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ScoreRuleResult")
    Optional<ScoreRule> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int insert(ScoreRule row) {
        return MyBatis3Utils.insert(this::insert, row, scoreRule, c ->
            c.map(id).toProperty("id")
            .map(ruleName).toProperty("ruleName")
            .map(rankedWinScore).toProperty("rankedWinScore")
            .map(rankedLoseScore).toProperty("rankedLoseScore")
            .map(rankedDrawScore).toProperty("rankedDrawScore")
            .map(casualWinScore).toProperty("casualWinScore")
            .map(casualLoseScore).toProperty("casualLoseScore")
            .map(casualDrawScore).toProperty("casualDrawScore")
            .map(privateWinScore).toProperty("privateWinScore")
            .map(privateLoseScore).toProperty("privateLoseScore")
            .map(privateDrawScore).toProperty("privateDrawScore")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int insertMultiple(Collection<ScoreRule> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, scoreRule, c ->
            c.map(id).toProperty("id")
            .map(ruleName).toProperty("ruleName")
            .map(rankedWinScore).toProperty("rankedWinScore")
            .map(rankedLoseScore).toProperty("rankedLoseScore")
            .map(rankedDrawScore).toProperty("rankedDrawScore")
            .map(casualWinScore).toProperty("casualWinScore")
            .map(casualLoseScore).toProperty("casualLoseScore")
            .map(casualDrawScore).toProperty("casualDrawScore")
            .map(privateWinScore).toProperty("privateWinScore")
            .map(privateLoseScore).toProperty("privateLoseScore")
            .map(privateDrawScore).toProperty("privateDrawScore")
            .map(description).toProperty("description")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int insertSelective(ScoreRule row) {
        return MyBatis3Utils.insert(this::insert, row, scoreRule, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(ruleName).toPropertyWhenPresent("ruleName", row::getRuleName)
            .map(rankedWinScore).toPropertyWhenPresent("rankedWinScore", row::getRankedWinScore)
            .map(rankedLoseScore).toPropertyWhenPresent("rankedLoseScore", row::getRankedLoseScore)
            .map(rankedDrawScore).toPropertyWhenPresent("rankedDrawScore", row::getRankedDrawScore)
            .map(casualWinScore).toPropertyWhenPresent("casualWinScore", row::getCasualWinScore)
            .map(casualLoseScore).toPropertyWhenPresent("casualLoseScore", row::getCasualLoseScore)
            .map(casualDrawScore).toPropertyWhenPresent("casualDrawScore", row::getCasualDrawScore)
            .map(privateWinScore).toPropertyWhenPresent("privateWinScore", row::getPrivateWinScore)
            .map(privateLoseScore).toPropertyWhenPresent("privateLoseScore", row::getPrivateLoseScore)
            .map(privateDrawScore).toPropertyWhenPresent("privateDrawScore", row::getPrivateDrawScore)
            .map(description).toPropertyWhenPresent("description", row::getDescription)
            .map(createdTime).toPropertyWhenPresent("createdTime", row::getCreatedTime)
            .map(updatedTime).toPropertyWhenPresent("updatedTime", row::getUpdatedTime)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default Optional<ScoreRule> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default List<ScoreRule> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default List<ScoreRule> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default Optional<ScoreRule> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, scoreRule, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    static UpdateDSL<UpdateModel> updateAllColumns(ScoreRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(ruleName).equalTo(row::getRuleName)
                .set(rankedWinScore).equalTo(row::getRankedWinScore)
                .set(rankedLoseScore).equalTo(row::getRankedLoseScore)
                .set(rankedDrawScore).equalTo(row::getRankedDrawScore)
                .set(casualWinScore).equalTo(row::getCasualWinScore)
                .set(casualLoseScore).equalTo(row::getCasualLoseScore)
                .set(casualDrawScore).equalTo(row::getCasualDrawScore)
                .set(privateWinScore).equalTo(row::getPrivateWinScore)
                .set(privateLoseScore).equalTo(row::getPrivateLoseScore)
                .set(privateDrawScore).equalTo(row::getPrivateDrawScore)
                .set(description).equalTo(row::getDescription)
                .set(createdTime).equalTo(row::getCreatedTime)
                .set(updatedTime).equalTo(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(ScoreRule row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(ruleName).equalToWhenPresent(row::getRuleName)
                .set(rankedWinScore).equalToWhenPresent(row::getRankedWinScore)
                .set(rankedLoseScore).equalToWhenPresent(row::getRankedLoseScore)
                .set(rankedDrawScore).equalToWhenPresent(row::getRankedDrawScore)
                .set(casualWinScore).equalToWhenPresent(row::getCasualWinScore)
                .set(casualLoseScore).equalToWhenPresent(row::getCasualLoseScore)
                .set(casualDrawScore).equalToWhenPresent(row::getCasualDrawScore)
                .set(privateWinScore).equalToWhenPresent(row::getPrivateWinScore)
                .set(privateLoseScore).equalToWhenPresent(row::getPrivateLoseScore)
                .set(privateDrawScore).equalToWhenPresent(row::getPrivateDrawScore)
                .set(description).equalToWhenPresent(row::getDescription)
                .set(createdTime).equalToWhenPresent(row::getCreatedTime)
                .set(updatedTime).equalToWhenPresent(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int updateByPrimaryKey(ScoreRule row) {
        return update(c ->
            c.set(ruleName).equalTo(row::getRuleName)
            .set(rankedWinScore).equalTo(row::getRankedWinScore)
            .set(rankedLoseScore).equalTo(row::getRankedLoseScore)
            .set(rankedDrawScore).equalTo(row::getRankedDrawScore)
            .set(casualWinScore).equalTo(row::getCasualWinScore)
            .set(casualLoseScore).equalTo(row::getCasualLoseScore)
            .set(casualDrawScore).equalTo(row::getCasualDrawScore)
            .set(privateWinScore).equalTo(row::getPrivateWinScore)
            .set(privateLoseScore).equalTo(row::getPrivateLoseScore)
            .set(privateDrawScore).equalTo(row::getPrivateDrawScore)
            .set(description).equalTo(row::getDescription)
            .set(createdTime).equalTo(row::getCreatedTime)
            .set(updatedTime).equalTo(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    default int updateByPrimaryKeySelective(ScoreRule row) {
        return update(c ->
            c.set(ruleName).equalToWhenPresent(row::getRuleName)
            .set(rankedWinScore).equalToWhenPresent(row::getRankedWinScore)
            .set(rankedLoseScore).equalToWhenPresent(row::getRankedLoseScore)
            .set(rankedDrawScore).equalToWhenPresent(row::getRankedDrawScore)
            .set(casualWinScore).equalToWhenPresent(row::getCasualWinScore)
            .set(casualLoseScore).equalToWhenPresent(row::getCasualLoseScore)
            .set(casualDrawScore).equalToWhenPresent(row::getCasualDrawScore)
            .set(privateWinScore).equalToWhenPresent(row::getPrivateWinScore)
            .set(privateLoseScore).equalToWhenPresent(row::getPrivateLoseScore)
            .set(privateDrawScore).equalToWhenPresent(row::getPrivateDrawScore)
            .set(description).equalToWhenPresent(row::getDescription)
            .set(createdTime).equalToWhenPresent(row::getCreatedTime)
            .set(updatedTime).equalToWhenPresent(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO score_rule" +
              " (`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") ScoreRule record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO score_rule" +
            " (`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<ScoreRule> records);

    @Insert({"<script>" +
            " INSERT INTO score_rule" +
              "(`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  rule_name = r.rule_name, ranked_win_score = r.ranked_win_score, ranked_lose_score = r.ranked_lose_score, ranked_draw_score = r.ranked_draw_score, casual_win_score = r.casual_win_score, casual_lose_score = r.casual_lose_score, casual_draw_score = r.casual_draw_score, private_win_score = r.private_win_score, private_lose_score = r.private_lose_score, private_draw_score = r.private_draw_score, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") ScoreRule record);

    @Insert({"<script>" +
            " INSERT INTO score_rule" +
            " (`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  rule_name = r.rule_name, ranked_win_score = r.ranked_win_score, ranked_lose_score = r.ranked_lose_score, ranked_draw_score = r.ranked_draw_score, casual_win_score = r.casual_win_score, casual_lose_score = r.casual_lose_score, casual_draw_score = r.casual_draw_score, private_win_score = r.private_win_score, private_lose_score = r.private_lose_score, private_draw_score = r.private_draw_score, description = r.description, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<ScoreRule> records);

    @Insert({"<script>" +
            " REPLACE INTO score_rule" +
              " (`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") ScoreRule record);

    @Insert({"<script>" +
            " REPLACE INTO score_rule" +
            " (`id`, `rule_name`, `ranked_win_score`, `ranked_lose_score`, `ranked_draw_score`, `casual_win_score`, `casual_lose_score`, `casual_draw_score`, `private_win_score`, `private_lose_score`, `private_draw_score`, `description`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.ruleName}, #{item.rankedWinScore}, #{item.rankedLoseScore}, #{item.rankedDrawScore}, #{item.casualWinScore}, #{item.casualLoseScore}, #{item.casualDrawScore}, #{item.privateWinScore}, #{item.privateLoseScore}, #{item.privateDrawScore}, #{item.description}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<ScoreRule> records);
}