-- =====================================================
-- Cleanup Old Leaderboard Rules
-- Removes leaderboard rules that have ended more than 7 days ago
-- Keeps TOTAL leaderboard (permanent)
-- =====================================================

-- 1. Show records that will be deleted
SELECT
    type,
    COUNT(*) AS records_to_delete,
    MIN(FROM_UNIXTIME(start_time)) AS earliest,
    MAX(FROM_UNIXTIME(end_time)) AS latest
FROM leaderboard_rule
WHERE end_time < UNIX_TIMESTAMP(NOW() - INTERVAL 7 DAY)
  AND type != 'TOTAL'
GROUP BY type;

-- 2. Delete old leaderboard rules (keeps last 7 days)
DELETE FROM leaderboard_rule
WHERE end_time < UNIX_TIMESTAMP(NOW() - INTERVAL 7 DAY)
  AND type != 'TOTAL';

-- 3. Verify remaining records
SELECT
    type,
    COUNT(*) AS total_records,
    MIN(FROM_UNIXTIME(start_time)) AS earliest_start,
    MAX(FROM_UNIXTIME(end_time)) AS latest_end
FROM leaderboard_rule
GROUP BY type
ORDER BY FIELD(type, 'TOTAL', 'DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL');
