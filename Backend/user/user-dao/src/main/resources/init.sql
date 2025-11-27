CREATE TABLE `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    `email` VARCHAR(128) DEFAULT '' COMMENT 'Bound email address',
    `nickname` VARCHAR(64) DEFAULT '' COMMENT 'User nickname',
    `password_hash` CHAR(60) NOT NULL COMMENT 'BCrypt password hash',
    `password_salt` CHAR(32) NOT NULL COMMENT 'UUID-based password salt',
    `avatar_url` VARCHAR(255) DEFAULT '' COMMENT 'Avatar URL',
    `avatar_base64` text COMMENT 'Avatar base64',
    `country` VARCHAR(64) DEFAULT '' COMMENT 'Country',
    `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT 'Gender (0=unknown, 1=male, 2=female)',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Account status (0=inactive, 1=active, 2=disabled, 3=deleted)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Core Info Table';

CREATE TABLE `user_token`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `refresh_token` VARCHAR(64) NOT NULL COMMENT 'Refresh Token (UUID)',
    `expires_at`    DATETIME    NOT NULL COMMENT 'Expiration time',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_refresh_token` (`refresh_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Token Table';
