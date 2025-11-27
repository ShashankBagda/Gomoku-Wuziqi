package com.goody.nus.se.gomoku.ranking.model.dao;

import static com.goody.nus.se.gomoku.ranking.model.dao.ScoreDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.ranking.model.entity.Score;
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
public interface ScoreMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Score>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    BasicColumn[] selectList = BasicColumn.columnList(id, leaderboardRuleId, userId, matchId, ruleId, scoreChange, finalScore, matchResult, createdTime, updatedTime);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ScoreResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="leaderboard_rule_id", property="leaderboardRuleId", jdbcType=JdbcType.BIGINT),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        @Result(column="match_id", property="matchId", jdbcType=JdbcType.BIGINT),
        @Result(column="rule_id", property="ruleId", jdbcType=JdbcType.BIGINT),
        @Result(column="score_change", property="scoreChange", jdbcType=JdbcType.INTEGER),
        @Result(column="final_score", property="finalScore", jdbcType=JdbcType.INTEGER),
        @Result(column="match_result", property="matchResult", jdbcType=JdbcType.CHAR),
        @Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<Score> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ScoreResult")
    Optional<Score> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int insert(Score row) {
        return MyBatis3Utils.insert(this::insert, row, score, c ->
            c.map(id).toProperty("id")
            .map(leaderboardRuleId).toProperty("leaderboardRuleId")
            .map(userId).toProperty("userId")
            .map(matchId).toProperty("matchId")
            .map(ruleId).toProperty("ruleId")
            .map(scoreChange).toProperty("scoreChange")
            .map(finalScore).toProperty("finalScore")
            .map(matchResult).toProperty("matchResult")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int insertMultiple(Collection<Score> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, score, c ->
            c.map(id).toProperty("id")
            .map(leaderboardRuleId).toProperty("leaderboardRuleId")
            .map(userId).toProperty("userId")
            .map(matchId).toProperty("matchId")
            .map(ruleId).toProperty("ruleId")
            .map(scoreChange).toProperty("scoreChange")
            .map(finalScore).toProperty("finalScore")
            .map(matchResult).toProperty("matchResult")
            .map(createdTime).toProperty("createdTime")
            .map(updatedTime).toProperty("updatedTime")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int insertSelective(Score row) {
        return MyBatis3Utils.insert(this::insert, row, score, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(leaderboardRuleId).toPropertyWhenPresent("leaderboardRuleId", row::getLeaderboardRuleId)
            .map(userId).toPropertyWhenPresent("userId", row::getUserId)
            .map(matchId).toPropertyWhenPresent("matchId", row::getMatchId)
            .map(ruleId).toPropertyWhenPresent("ruleId", row::getRuleId)
            .map(scoreChange).toPropertyWhenPresent("scoreChange", row::getScoreChange)
            .map(finalScore).toPropertyWhenPresent("finalScore", row::getFinalScore)
            .map(matchResult).toPropertyWhenPresent("matchResult", row::getMatchResult)
            .map(createdTime).toPropertyWhenPresent("createdTime", row::getCreatedTime)
            .map(updatedTime).toPropertyWhenPresent("updatedTime", row::getUpdatedTime)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default Optional<Score> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default List<Score> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default List<Score> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default Optional<Score> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, score, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    static UpdateDSL<UpdateModel> updateAllColumns(Score row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(leaderboardRuleId).equalTo(row::getLeaderboardRuleId)
                .set(userId).equalTo(row::getUserId)
                .set(matchId).equalTo(row::getMatchId)
                .set(ruleId).equalTo(row::getRuleId)
                .set(scoreChange).equalTo(row::getScoreChange)
                .set(finalScore).equalTo(row::getFinalScore)
                .set(matchResult).equalTo(row::getMatchResult)
                .set(createdTime).equalTo(row::getCreatedTime)
                .set(updatedTime).equalTo(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(Score row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(leaderboardRuleId).equalToWhenPresent(row::getLeaderboardRuleId)
                .set(userId).equalToWhenPresent(row::getUserId)
                .set(matchId).equalToWhenPresent(row::getMatchId)
                .set(ruleId).equalToWhenPresent(row::getRuleId)
                .set(scoreChange).equalToWhenPresent(row::getScoreChange)
                .set(finalScore).equalToWhenPresent(row::getFinalScore)
                .set(matchResult).equalToWhenPresent(row::getMatchResult)
                .set(createdTime).equalToWhenPresent(row::getCreatedTime)
                .set(updatedTime).equalToWhenPresent(row::getUpdatedTime);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int updateByPrimaryKey(Score row) {
        return update(c ->
            c.set(leaderboardRuleId).equalTo(row::getLeaderboardRuleId)
            .set(userId).equalTo(row::getUserId)
            .set(matchId).equalTo(row::getMatchId)
            .set(ruleId).equalTo(row::getRuleId)
            .set(scoreChange).equalTo(row::getScoreChange)
            .set(finalScore).equalTo(row::getFinalScore)
            .set(matchResult).equalTo(row::getMatchResult)
            .set(createdTime).equalTo(row::getCreatedTime)
            .set(updatedTime).equalTo(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    default int updateByPrimaryKeySelective(Score row) {
        return update(c ->
            c.set(leaderboardRuleId).equalToWhenPresent(row::getLeaderboardRuleId)
            .set(userId).equalToWhenPresent(row::getUserId)
            .set(matchId).equalToWhenPresent(row::getMatchId)
            .set(ruleId).equalToWhenPresent(row::getRuleId)
            .set(scoreChange).equalToWhenPresent(row::getScoreChange)
            .set(finalScore).equalToWhenPresent(row::getFinalScore)
            .set(matchResult).equalToWhenPresent(row::getMatchResult)
            .set(createdTime).equalToWhenPresent(row::getCreatedTime)
            .set(updatedTime).equalToWhenPresent(row::getUpdatedTime)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO score" +
              " (`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") Score record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO score" +
            " (`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<Score> records);

    @Insert({"<script>" +
            " INSERT INTO score" +
              "(`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  leaderboard_rule_id = r.leaderboard_rule_id, user_id = r.user_id, match_id = r.match_id, rule_id = r.rule_id, score_change = r.score_change, final_score = r.final_score, match_result = r.match_result, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") Score record);

    @Insert({"<script>" +
            " INSERT INTO score" +
            " (`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  leaderboard_rule_id = r.leaderboard_rule_id, user_id = r.user_id, match_id = r.match_id, rule_id = r.rule_id, score_change = r.score_change, final_score = r.final_score, match_result = r.match_result, created_time = r.created_time, updated_time = r.updated_time" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<Score> records);

    @Insert({"<script>" +
            " REPLACE INTO score" +
              " (`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " VALUES" +
              "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") Score record);

    @Insert({"<script>" +
            " REPLACE INTO score" +
            " (`id`, `leaderboard_rule_id`, `user_id`, `match_id`, `rule_id`, `score_change`, `final_score`, `match_result`, `created_time`, `updated_time`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.leaderboardRuleId}, #{item.userId}, #{item.matchId}, #{item.ruleId}, #{item.scoreChange}, #{item.finalScore}, #{item.matchResult}, #{item.createdTime}, #{item.updatedTime})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<Score> records);
}