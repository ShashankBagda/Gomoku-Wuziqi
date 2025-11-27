# Leaderboard Management Scripts

这个目录包含了用于管理排行榜数据的SQL脚本。

## 📋 脚本说明

### 1. `init_leaderboard_data.sql` - 初始化脚本（推荐使用）

**用途**：完整初始化排行榜系统，包括所有类型的排行榜

**包含内容**：
- ✅ TOTAL 排行榜（永久）
- ✅ DAILY 排行榜（未来60天）
- ✅ WEEKLY 排行榜（未来10周）
- ✅ MONTHLY 排行榜（未来3个月）
- ✅ SEASONAL 排行榜（当前季度+下一季度）
- ✅ 验证报告（自动显示当前活跃的排行榜）

**使用场景**：
- 首次部署系统
- 重新初始化排行榜数据
- 定期维护（如每周/每月执行一次）

**执行方式**：
```bash
mysql -u your_username -p your_database < init_leaderboard_data.sql
```

或在MySQL客户端中：
```sql
source /path/to/init_leaderboard_data.sql;
```

---

### 2. `generate_future_leaderboards.sql` - 生成未来排行榜

**用途**：生成未来两个月的排行榜数据（更详细的版本）

**包含内容**：
- DAILY：未来60天
- WEEKLY：未来10周
- MONTHLY：未来3个月
- SEASONAL：当前和下一季度
- 详细的验证查询

**使用场景**：
- 需要单独生成未来排行榜
- 想要查看详细的生成报告
- 自定义时间范围

---

### 3. `cleanup_old_leaderboards.sql` - 清理旧数据

**用途**：删除7天前结束的旧排行榜记录

**包含内容**：
- 删除前预览（显示将要删除的记录）
- 删除旧的 DAILY/WEEKLY/MONTHLY/SEASONAL 记录
- 保留 TOTAL 排行榜（永久）
- 删除后验证报告

**使用场景**：
- 定期清理数据库
- 防止排行榜表过大
- 维护数据库性能

**建议执行频率**：每周或每月执行一次

---

## 🚀 快速开始

### 首次部署

1. 执行初始化脚本：
   ```bash
   mysql -u root -p gomoku < init_leaderboard_data.sql
   ```

2. 验证数据：
   ```sql
   -- 查看所有排行榜类型及数量
   SELECT type, COUNT(*) FROM leaderboard_rule GROUP BY type;

   -- 查看当前活跃的排行榜
   SELECT * FROM leaderboard_rule
   WHERE UNIX_TIMESTAMP(NOW()) BETWEEN start_time AND end_time;
   ```

### 定期维护（建议每周执行）

1. 清理旧数据：
   ```bash
   mysql -u root -p gomoku < cleanup_old_leaderboards.sql
   ```

2. 重新生成未来数据：
   ```bash
   mysql -u root -p gomoku < init_leaderboard_data.sql
   ```

---

## 📊 排行榜类型说明

| 类型 | 时间范围 | 重置周期 | 说明 |
|------|----------|----------|------|
| **TOTAL** | 永久 | 从不重置 | 玩家的总排名，累计所有游戏数据 |
| **SEASONAL** | 3个月（一个季度） | 每季度 | Q1: 1-3月, Q2: 4-6月, Q3: 7-9月, Q4: 10-12月 |
| **MONTHLY** | 1个月 | 每月1日 | 当月排行榜 |
| **WEEKLY** | 7天 | 每周一 | 本周排行榜（周一到周日） |
| **DAILY** | 1天 | 每日凌晨 | 当日排行榜 |

---

## 🔍 验证查询

### 检查当前活跃的排行榜

```sql
SELECT
    id,
    type,
    FROM_UNIXTIME(start_time) AS start_date,
    FROM_UNIXTIME(end_time) AS end_date,
    description
FROM leaderboard_rule
WHERE UNIX_TIMESTAMP(NOW()) BETWEEN start_time AND end_time
ORDER BY FIELD(type, 'TOTAL', 'DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL');
```

### 检查未来7天的排行榜

```sql
SELECT
    type,
    FROM_UNIXTIME(start_time) AS start_date,
    description
FROM leaderboard_rule
WHERE start_time BETWEEN UNIX_TIMESTAMP(NOW()) AND UNIX_TIMESTAMP(NOW() + INTERVAL 7 DAY)
ORDER BY start_time;
```

### 统计各类型排行榜数量

```sql
SELECT
    type,
    COUNT(*) AS total,
    COUNT(CASE WHEN end_time < UNIX_TIMESTAMP(NOW()) THEN 1 END) AS expired,
    COUNT(CASE WHEN UNIX_TIMESTAMP(NOW()) BETWEEN start_time AND end_time THEN 1 END) AS active,
    COUNT(CASE WHEN start_time > UNIX_TIMESTAMP(NOW()) THEN 1 END) AS upcoming
FROM leaderboard_rule
GROUP BY type;
```

---

## ⚙️ 自动化建议

### Cron Job 配置（Linux）

编辑 crontab:
```bash
crontab -e
```

添加定时任务：
```bash
# 每周一凌晨2点清理旧数据并生成新数据
0 2 * * 1 mysql -u root -p'your_password' gomoku < /path/to/cleanup_old_leaderboards.sql
5 2 * * 1 mysql -u root -p'your_password' gomoku < /path/to/init_leaderboard_data.sql
```

### Windows 任务计划程序

1. 打开"任务计划程序"
2. 创建基本任务
3. 设置触发器（每周一次）
4. 操作：运行批处理文件
5. 批处理文件内容：
   ```batch
   @echo off
   mysql -u root -p"your_password" gomoku < "D:\path\to\cleanup_old_leaderboards.sql"
   mysql -u root -p"your_password" gomoku < "D:\path\to\init_leaderboard_data.sql"
   ```

---

## 🛠️ 故障排查

### 问题1：没有当前活跃的DAILY排行榜

**原因**：时区问题或数据未生成

**解决**：
```sql
-- 检查当前时间
SELECT NOW(), UNIX_TIMESTAMP(NOW());

-- 重新生成数据
source init_leaderboard_data.sql;
```

### 问题2：排行榜数据过多

**原因**：未定期清理旧数据

**解决**：
```sql
-- 执行清理脚本
source cleanup_old_leaderboards.sql;
```

### 问题3：WEEKLY排行榜周期不正确

**原因**：WEEKDAY计算问题（周一为0，周日为6）

**验证**：
```sql
SELECT
    WEEKDAY(NOW()) AS current_weekday,
    DATE_SUB(NOW(), INTERVAL WEEKDAY(NOW()) DAY) AS week_start,
    DATE_ADD(DATE_SUB(NOW(), INTERVAL WEEKDAY(NOW()) DAY), INTERVAL 6 DAY) AS week_end;
```

---

## 📝 注意事项

1. **备份数据**：执行清理脚本前建议备份数据库
2. **时区设置**：确保MySQL时区设置正确
3. **权限检查**：确保数据库用户有 INSERT/DELETE 权限
4. **重复执行**：所有脚本使用 `ON DUPLICATE KEY UPDATE`，可以安全重复执行
5. **TOTAL排行榜**：永远不会被清理脚本删除

---

## 📚 相关文档

- [Ranking System Design](./01-usecase-diagram.puml)
- [Sequence Diagrams](./03-sequence-diagram.puml)
- [Database Schema](../ranking-dao/src/main/resources/schema.sql)

---

## 🤝 贡献

如果你发现问题或有改进建议，请提交 Issue 或 Pull Request。

---

**最后更新**：2025-11-04
**维护者**：Ranking Team
