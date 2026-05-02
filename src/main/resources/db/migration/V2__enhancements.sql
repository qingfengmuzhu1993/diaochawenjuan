-- V2: Gamification persistence + badge system

CREATE TABLE IF NOT EXISTS sign_in_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sign_date DATE NOT NULL,
    streak_days INT DEFAULT 1,
    points_earned INT DEFAULT 5,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, sign_date),
    INDEX idx_user_sign (user_id, sign_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录';

CREATE TABLE IF NOT EXISTS badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    icon VARCHAR(50),
    condition_type VARCHAR(30) NOT NULL,
    condition_value INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勋章定义';

CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    INDEX idx_user_badges (user_id),
    FOREIGN KEY (badge_id) REFERENCES badges(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户勋章';

ALTER TABLE responses ADD COLUMN IF NOT EXISTS referrer_id BIGINT AFTER channel;

INSERT IGNORE INTO badges (name, description, icon, condition_type, condition_value) VALUES
('百答达人', '完成100份问卷回答', 'medal', 'total_responses', 100),
('质量之星', '连续50份回答审核通过', 'star', 'consecutive_approved', 50),
('闪电侠', '平均完成速度排名前10%', 'bolt', 'avg_speed', 10),
('签到达人', '连续签到30天', 'calendar', 'consecutive_signin', 30),
('金牌答手', '累计收益超过1000元', 'coin', 'total_earnings', 1000),
('首批用户', '平台上线首月注册', 'rocket', 'early_user', 1);

CREATE TABLE IF NOT EXISTS points_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exchange_type VARCHAR(30) NOT NULL,
    points_spent INT NOT NULL,
    reward VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_type (user_id, exchange_type),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分使用记录';
