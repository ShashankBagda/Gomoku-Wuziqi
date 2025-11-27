-- =====================================================
-- Initialize Leaderboard System
-- Complete setup for ranking system with all required data
-- =====================================================

-- =====================================================
-- Step 1: Create TOTAL Leaderboard (Permanent)
-- =====================================================
INSERT INTO leaderboard_rule (start_time, end_time, type, rule_id, description, created_time, updated_time)
VALUES (
    0,
    2051222400, -- 2035-01-01 00:00:00
    'TOTAL',
    1,
    'Permanent total leaderboard',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_time = NOW();

-- =====================================================
-- Step 2: Generate Current + Future Leaderboards
-- =====================================================

-- DAILY: Next 60 days from today
INSERT INTO leaderboard_rule (start_time, end_time, type, rule_id, description, created_time, updated_time)
SELECT
    UNIX_TIMESTAMP(DATE(NOW() + INTERVAL day_offset DAY)) AS start_time,
    UNIX_TIMESTAMP(DATE(NOW() + INTERVAL day_offset DAY) + INTERVAL 1 DAY - INTERVAL 1 SECOND) AS end_time,
    'DAILY' AS type,
    1 AS rule_id,
    CONCAT('Daily leaderboard ', DATE_FORMAT(NOW() + INTERVAL day_offset DAY, '%Y-%m-%d')) AS description,
    NOW() AS created_time,
    NOW() AS updated_time
FROM (
    SELECT 0 AS day_offset UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL
    SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL
    SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL
    SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL
    SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL
    SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL
    SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL
    SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL
    SELECT 50 UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL
    SELECT 55 UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59
) AS days
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- WEEKLY: Next 10 weeks from today
INSERT INTO leaderboard_rule (start_time, end_time, type, rule_id, description, created_time, updated_time)
SELECT
    UNIX_TIMESTAMP(DATE_SUB(NOW() + INTERVAL week_offset WEEK, INTERVAL WEEKDAY(NOW() + INTERVAL week_offset WEEK) DAY)) AS start_time,
    UNIX_TIMESTAMP(DATE_SUB(NOW() + INTERVAL week_offset WEEK, INTERVAL WEEKDAY(NOW() + INTERVAL week_offset WEEK) DAY) + INTERVAL 7 DAY - INTERVAL 1 SECOND) AS end_time,
    'WEEKLY' AS type,
    1 AS rule_id,
    CONCAT('Weekly leaderboard Week ', WEEK(NOW() + INTERVAL week_offset WEEK, 1), ' ', YEAR(NOW() + INTERVAL week_offset WEEK)) AS description,
    NOW() AS created_time,
    NOW() AS updated_time
FROM (
    SELECT 0 AS week_offset UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) AS weeks
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- MONTHLY: Next 3 months from today
INSERT INTO leaderboard_rule (start_time, end_time, type, rule_id, description, created_time, updated_time)
SELECT
    UNIX_TIMESTAMP(DATE_FORMAT(NOW() + INTERVAL month_offset MONTH, '%Y-%m-01')) AS start_time,
    UNIX_TIMESTAMP(LAST_DAY(NOW() + INTERVAL month_offset MONTH) + INTERVAL 1 DAY - INTERVAL 1 SECOND) AS end_time,
    'MONTHLY' AS type,
    1 AS rule_id,
    CONCAT('Monthly leaderboard ', DATE_FORMAT(NOW() + INTERVAL month_offset MONTH, '%Y-%m')) AS description,
    NOW() AS created_time,
    NOW() AS updated_time
FROM (
    SELECT 0 AS month_offset UNION ALL SELECT 1 UNION ALL SELECT 2
) AS months
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- SEASONAL: Current and next quarter
INSERT INTO leaderboard_rule (start_time, end_time, type, rule_id, description, created_time, updated_time)
SELECT
    UNIX_TIMESTAMP(
        CASE
            WHEN QUARTER(NOW()) = 1 THEN CONCAT(YEAR(NOW()), '-01-01')
            WHEN QUARTER(NOW()) = 2 THEN CONCAT(YEAR(NOW()), '-04-01')
            WHEN QUARTER(NOW()) = 3 THEN CONCAT(YEAR(NOW()), '-07-01')
            WHEN QUARTER(NOW()) = 4 THEN CONCAT(YEAR(NOW()), '-10-01')
        END
    ) AS start_time,
    UNIX_TIMESTAMP(
        CASE
            WHEN QUARTER(NOW()) = 1 THEN CONCAT(YEAR(NOW()), '-03-31 23:59:59')
            WHEN QUARTER(NOW()) = 2 THEN CONCAT(YEAR(NOW()), '-06-30 23:59:59')
            WHEN QUARTER(NOW()) = 3 THEN CONCAT(YEAR(NOW()), '-09-30 23:59:59')
            WHEN QUARTER(NOW()) = 4 THEN CONCAT(YEAR(NOW()), '-12-31 23:59:59')
        END
    ) AS end_time,
    'SEASONAL' AS type,
    1 AS rule_id,
    CONCAT('Seasonal leaderboard Q', QUARTER(NOW()), ' ', YEAR(NOW())) AS description,
    NOW() AS created_time,
    NOW() AS updated_time
UNION ALL
SELECT
    UNIX_TIMESTAMP(
        CASE
            WHEN QUARTER(NOW()) = 1 THEN CONCAT(YEAR(NOW()), '-04-01')
            WHEN QUARTER(NOW()) = 2 THEN CONCAT(YEAR(NOW()), '-07-01')
            WHEN QUARTER(NOW()) = 3 THEN CONCAT(YEAR(NOW()), '-10-01')
            WHEN QUARTER(NOW()) = 4 THEN CONCAT(YEAR(NOW()) + 1, '-01-01')
        END
    ) AS start_time,
    UNIX_TIMESTAMP(
        CASE
            WHEN QUARTER(NOW()) = 1 THEN CONCAT(YEAR(NOW()), '-06-30 23:59:59')
            WHEN QUARTER(NOW()) = 2 THEN CONCAT(YEAR(NOW()), '-09-30 23:59:59')
            WHEN QUARTER(NOW()) = 3 THEN CONCAT(YEAR(NOW()), '-12-31 23:59:59')
            WHEN QUARTER(NOW()) = 4 THEN CONCAT(YEAR(NOW()) + 1, '-03-31 23:59:59')
        END
    ) AS end_time,
    'SEASONAL' AS type,
    1 AS rule_id,
    CONCAT(
        'Seasonal leaderboard Q',
        CASE WHEN QUARTER(NOW()) = 4 THEN 1 ELSE QUARTER(NOW()) + 1 END,
        ' ',
        CASE WHEN QUARTER(NOW()) = 4 THEN YEAR(NOW()) + 1 ELSE YEAR(NOW()) END
    ) AS description,
    NOW() AS created_time,
    NOW() AS updated_time
ON DUPLICATE KEY UPDATE updated_time = NOW();

-- =====================================================
-- Step 3: Verification Report
-- =====================================================
SELECT '=== Leaderboard Summary ===' AS report;

SELECT
    type,
    COUNT(*) AS total_records,
    MIN(FROM_UNIXTIME(start_time)) AS earliest_start,
    MAX(FROM_UNIXTIME(end_time)) AS latest_end
FROM leaderboard_rule
GROUP BY type
ORDER BY FIELD(type, 'TOTAL', 'DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL');

SELECT '=== Current Active Leaderboards ===' AS report;

SELECT
    id,
    type,
    FROM_UNIXTIME(start_time) AS start_date,
    FROM_UNIXTIME(end_time) AS end_date,
    description
FROM leaderboard_rule
WHERE UNIX_TIMESTAMP(NOW()) BETWEEN start_time AND end_time
ORDER BY FIELD(type, 'TOTAL', 'DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL');

SELECT '=== Upcoming Leaderboards (Next 7 Days) ===' AS report;

SELECT
    type,
    FROM_UNIXTIME(start_time) AS start_date,
    FROM_UNIXTIME(end_time) AS end_date,
    description
FROM leaderboard_rule
WHERE start_time BETWEEN UNIX_TIMESTAMP(NOW()) AND UNIX_TIMESTAMP(NOW() + INTERVAL 7 DAY)
ORDER BY start_time, FIELD(type, 'DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL')
LIMIT 20;
