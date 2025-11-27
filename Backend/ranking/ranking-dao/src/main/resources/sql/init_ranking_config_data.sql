drop table if exists test_ranking.leaderboard_rule;
drop table if exists test_ranking.level;
drop table if exists test_ranking.level_rule;
drop table if exists test_ranking.ranking;
drop table if exists test_ranking.score;
drop table if exists test_ranking.score_rule;
create table test_ranking.leaderboard_rule
(
    id           bigint unsigned auto_increment comment 'Primary key'
        primary key,
    start_time   int          default 0                                   not null comment 'Leaderboard start time (Unix timestamp)',
    end_time     int          default 0                                   not null comment 'Leaderboard end time (Unix timestamp)',
    type         enum ('DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL', 'TOTAL') not null comment 'Leaderboard type',
    rule_id      bigint unsigned                                          not null comment 'Linked score rule ID',
    description  varchar(255) default ''                                  not null comment 'Leaderboard description',
    created_time datetime     default CURRENT_TIMESTAMP                   not null comment 'Creation time',
    updated_time datetime     default CURRENT_TIMESTAMP                   not null on update CURRENT_TIMESTAMP comment 'Update time'
)
    comment 'Leaderboard rule configuration table (daily/monthly/seasonal/total)' charset = utf8mb4;

create index idx_rule_id
    on test_ranking.leaderboard_rule (rule_id);

create index idx_type
    on test_ranking.leaderboard_rule (type);

create table test_ranking.level
(
    id           bigint unsigned auto_increment comment 'Level ID'
        primary key,
    exp_required int unsigned                       not null comment 'Experience required to reach the next level',
    created_time datetime default CURRENT_TIMESTAMP not null comment 'Creation time',
    updated_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time'
)
    comment 'Level experience configuration table' charset = utf8mb4;

create table test_ranking.level_rule
(
    id           bigint unsigned auto_increment comment 'Primary key'
        primary key,
    mode_type    enum ('RANKED', 'CASUAL', 'PRIVATE')   not null comment 'Game mode type',
    match_result enum ('WIN', 'LOSE', 'DRAW')           not null comment 'Match result',
    exp_value    int          default 0                 not null comment 'Experience value change',
    description  varchar(255) default ''                not null comment 'Rule description',
    created_time datetime     default CURRENT_TIMESTAMP not null comment 'Creation time',
    updated_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time',
    constraint uniq_level_mode_result
        unique (mode_type, match_result)
)
    comment 'Experience rule configuration table' charset = utf8mb4;

create index idx_match_result
    on test_ranking.level_rule (match_result);

create index idx_mode_type
    on test_ranking.level_rule (mode_type);

create table test_ranking.ranking
(
    id                  bigint unsigned auto_increment comment 'Primary key'
        primary key,
    user_id             bigint unsigned                        not null comment 'Player ID',
    leaderboard_rule_id bigint unsigned                        not null comment 'Leaderboard ID',
    total_exp           int unsigned default '0'               not null comment 'Player total experience',
    level_id            bigint unsigned                        not null comment 'Current level ID',
    current_total_score int unsigned default '0'               not null comment 'Current total score (only changes in ranked mode)',
    rank_position       int unsigned default '0'               not null comment 'Current ranking position (refresh periodically)',
    created_time        datetime     default CURRENT_TIMESTAMP not null comment 'Creation time',
    updated_time        datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time'
)
    comment 'Player overall ranking table' charset = utf8mb4;

create index idx_leaderboard_rule_id
    on test_ranking.ranking (leaderboard_rule_id);

create index idx_rank_position
    on test_ranking.ranking (rank_position);

create index idx_user_id
    on test_ranking.ranking (user_id);

create table test_ranking.score
(
    id                  bigint unsigned auto_increment comment 'Primary key'
        primary key,
    leaderboard_rule_id bigint unsigned                    not null comment 'Leaderboard ID',
    user_id             bigint unsigned                    not null comment 'Player ID',
    match_id            bigint unsigned                    not null comment 'Match ID (from match module)',
    rule_id             bigint unsigned                    not null comment 'Score rule ID (from score_rule)',
    score_change        int                                not null comment 'Score change in this match',
    final_score         int unsigned                       not null comment 'Player total score after match',
    match_result        enum ('WIN', 'LOSE', 'DRAW')       not null comment 'Match result',
    created_time        datetime default CURRENT_TIMESTAMP not null comment 'Creation time',
    updated_time        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time'
)
    comment 'Ranked mode match score record table' charset = utf8mb4;

create index idx_created_time
    on test_ranking.score (created_time);

create index idx_match_id
    on test_ranking.score (match_id);

create index idx_user_id
    on test_ranking.score (user_id);

-- Flattened score_rule table
create table score_rule
(
    id                  bigint unsigned auto_increment comment 'Primary key'
        primary key,
    rule_name           varchar(64)  not null comment 'Rule name (e.g., Standard, Season1, etc.)',

    -- RANKED mode scores
    ranked_win_score    int default 0 not null comment 'RANKED mode WIN score change',
    ranked_lose_score   int default 0 not null comment 'RANKED mode LOSE score change',
    ranked_draw_score   int default 0 not null comment 'RANKED mode DRAW score change',

    -- CASUAL mode scores
    casual_win_score    int default 0 not null comment 'CASUAL mode WIN score change',
    casual_lose_score   int default 0 not null comment 'CASUAL mode LOSE score change',
    casual_draw_score   int default 0 not null comment 'CASUAL mode DRAW score change',

    -- PRIVATE mode scores
    private_win_score   int default 0 not null comment 'PRIVATE mode WIN score change',
    private_lose_score  int default 0 not null comment 'PRIVATE mode LOSE score change',
    private_draw_score  int default 0 not null comment 'PRIVATE mode DRAW score change',

    description         varchar(255) default '' not null comment 'Rule description',
    created_time        datetime default CURRENT_TIMESTAMP not null comment 'Creation time',
    updated_time        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time'
)
    comment 'Score rule configuration table (flattened structure for all modes and results)' charset = utf8mb4;

-- ========================================
-- Ranking System Configuration Data Initialization
-- ========================================
-- This script initializes the base configuration data for the ranking system
-- including levels, level rules, score rules, and leaderboard rules.
-- These are system-level configuration data that should persist across tests.
-- ========================================

-- 1. Initialize Level definitions
INSERT INTO level (id, exp_required, created_time, updated_time)
VALUES (1, 0, NOW(), NOW()),
       (2, 10, NOW(), NOW()),
       (3, 30, NOW(), NOW()),
       (4, 60, NOW(), NOW()),
       (5, 100, NOW(), NOW()),
       (6, 150, NOW(), NOW()),
       (7, 210, NOW(), NOW()),
       (8, 280, NOW(), NOW()),
       (9, 360, NOW(), NOW()),
       (10, 500, NOW(), NOW())
ON DUPLICATE KEY UPDATE exp_required = VALUES(exp_required),
                        updated_time = NOW();

-- 2. Initialize Level Rules (Experience rewards)
INSERT INTO level_rule (id, mode_type, match_result, exp_value, description, created_time, updated_time)
VALUES (1, 'RANKED', 'WIN', 50, 'Ranked mode win experience reward', NOW(), NOW()),
       (2, 'RANKED', 'LOSE', 10, 'Ranked mode lose experience reward', NOW(), NOW()),
       (3, 'RANKED', 'DRAW', 20, 'Ranked mode draw experience reward', NOW(), NOW()),
       (4, 'CASUAL', 'WIN', 20, 'Casual mode win experience reward', NOW(), NOW()),
       (5, 'CASUAL', 'LOSE', 5, 'Casual mode lose experience reward', NOW(), NOW()),
       (6, 'CASUAL', 'DRAW', 10, 'Casual mode draw experience reward', NOW(), NOW()),
       (7, 'PRIVATE', 'WIN', 15, 'Private room win experience reward', NOW(), NOW()),
       (8, 'PRIVATE', 'LOSE', 3, 'Private room lose experience reward', NOW(), NOW()),
       (9, 'PRIVATE', 'DRAW', 8, 'Private room draw experience reward', NOW(), NOW())
ON DUPLICATE KEY UPDATE exp_value    = VALUES(exp_value),
                        description  = VALUES(description),
                        updated_time = NOW();

-- 3. Initialize Score Rules (Score changes for RANKED mode only)
INSERT INTO score_rule (
    id, rule_name,
    ranked_win_score, ranked_lose_score, ranked_draw_score,
    casual_win_score, casual_lose_score, casual_draw_score,
    private_win_score, private_lose_score, private_draw_score,
    description
) VALUES (
 1, 'Standard',
 25, -15, 0,  -- RANKED: win +25, lose -15, draw 0
 0, 0, 0,     -- CASUAL: no score change
 0, 0, 0,     -- PRIVATE: no score change
 'Standard scoring rule used by all leaderboards'
);



-- 4. Initialize Leaderboard Rules
-- Note: Using far future timestamps (year 2099) for permanent leaderboards

-- TOTAL leaderboard (permanent)
INSERT INTO leaderboard_rule (id, type, rule_id, start_time, end_time, description, created_time, updated_time)
VALUES (1, 'TOTAL', 1, 0, 4102444800 / 2, 'Permanent total leaderboard', NOW(), NOW())
ON DUPLICATE KEY UPDATE start_time   = VALUES(start_time),
                        end_time     = VALUES(end_time),
                        description  = VALUES(description),
                        updated_time = NOW();

-- WEEKLY leaderboards (4 weeks starting from this week's Monday)
-- ID range: 10-13
INSERT INTO leaderboard_rule (id, type, rule_id, start_time, end_time, description, created_time, updated_time)
VALUES (10, 'WEEKLY', 1,
        UNIX_TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)),
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)) - 1,
        CONCAT('Weekly leaderboard Week ', WEEK(CURDATE(), 1), ' ', YEAR(CURDATE())),
        NOW(), NOW()),
       (11, 'WEEKLY', 1,
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 14 DAY)) - 1,
        CONCAT('Weekly leaderboard Week ', WEEK(DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1), ' ',
               YEAR(DATE_ADD(CURDATE(), INTERVAL 7 DAY))),
        NOW(), NOW()),
       (12, 'WEEKLY', 1,
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 14 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 21 DAY)) - 1,
        CONCAT('Weekly leaderboard Week ', WEEK(DATE_ADD(CURDATE(), INTERVAL 14 DAY), 1), ' ',
               YEAR(DATE_ADD(CURDATE(), INTERVAL 14 DAY))),
        NOW(), NOW()),
       (13, 'WEEKLY', 1,
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 21 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 28 DAY)) - 1,
        CONCAT('Weekly leaderboard Week ', WEEK(DATE_ADD(CURDATE(), INTERVAL 21 DAY), 1), ' ',
               YEAR(DATE_ADD(CURDATE(), INTERVAL 21 DAY))),
        NOW(), NOW())
ON DUPLICATE KEY UPDATE start_time   = VALUES(start_time),
                        end_time     = VALUES(end_time),
                        description  = VALUES(description),
                        updated_time = NOW();

-- MONTHLY leaderboard (current month)
INSERT INTO leaderboard_rule (id, type, rule_id, start_time, end_time, description, created_time, updated_time)
VALUES (2, 'MONTHLY', 1, UNIX_TIMESTAMP(DATE_FORMAT(NOW(), '%Y-%m-01')),
        UNIX_TIMESTAMP(LAST_DAY(NOW()) + INTERVAL 1 DAY) - 1, 'Monthly leaderboard (current month)', NOW(), NOW())
ON DUPLICATE KEY UPDATE start_time   = VALUES(start_time),
                        end_time     = VALUES(end_time),
                        description  = VALUES(description),
                        updated_time = NOW();

-- SEASONAL leaderboard (Q1 2025)
INSERT INTO leaderboard_rule (id, type, rule_id, start_time, end_time, description, created_time, updated_time)
VALUES (3, 'SEASONAL', 1, UNIX_TIMESTAMP('2025-10-01'), UNIX_TIMESTAMP('2026-01-01') - 1,
        'Seasonal leaderboard (Q4 2025)', NOW(), NOW())
ON DUPLICATE KEY UPDATE start_time   = VALUES(start_time),
                        end_time     = VALUES(end_time),
                        description  = VALUES(description),
                        updated_time = NOW();

-- DAILY leaderboards (30 days starting from today)
-- ID range: 100-129
INSERT INTO leaderboard_rule (id, type, rule_id, start_time, end_time, description, created_time, updated_time)
VALUES (100, 'DAILY', 1, UNIX_TIMESTAMP(CURDATE()), UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(CURDATE(), '%Y-%m-%d')), NOW(), NOW()),
       (101, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (102, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (103, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (104, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (105, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (106, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (107, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (108, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 8 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 9 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (109, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 9 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 10 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (110, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 10 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 11 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (111, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 11 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 12 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 11 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (112, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 12 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 13 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (113, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 13 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 14 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 13 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (114, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 15 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 14 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (115, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 15 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 16 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 15 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (116, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 16 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 17 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 16 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (117, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 17 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 18 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 17 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (118, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 18 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 19 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 18 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (119, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 19 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 20 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 19 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (120, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 21 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 20 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (121, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 22 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 21 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (122, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 22 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 23 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 22 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (123, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 23 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 24 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 23 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (124, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 24 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 25 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 24 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (125, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 25 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 26 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 25 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (126, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 26 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 27 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 26 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (127, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 27 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 28 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 27 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (128, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 28 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 29 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 28 DAY), '%Y-%m-%d')), NOW(), NOW()),
       (129, 'DAILY', 1, UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 29 DAY)),
        UNIX_TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 30 DAY)) - 1,
        CONCAT('Daily leaderboard ', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 29 DAY), '%Y-%m-%d')), NOW(), NOW())
ON DUPLICATE KEY UPDATE start_time   = VALUES(start_time),
                        end_time     = VALUES(end_time),
                        description  = VALUES(description),
                        updated_time = NOW();

-- ========================================
-- Verification queries (commented out)
-- ========================================
-- SELECT COUNT(*) AS level_count FROM level;
-- SELECT COUNT(*) AS level_rule_count FROM level_rule;
-- SELECT COUNT(*) AS score_rule_count FROM score_rule;
-- SELECT COUNT(*) AS leaderboard_rule_count FROM leaderboard_rule;
