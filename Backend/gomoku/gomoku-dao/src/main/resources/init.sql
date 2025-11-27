CREATE TABLE `game_room` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Room ID',
  `room_code` VARCHAR(32) NOT NULL COMMENT 'Room code for joining',
  `player1_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'First player user ID',
  `player2_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Second player user ID',
  `type` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Match type (0=casual, 1=ranked)',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Room status (0=waiting, 1=matched, 2=playing, 3=finished)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `uk_room_code` (`room_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Game Room Table';
