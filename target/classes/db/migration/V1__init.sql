-- Smart Survey Ecosystem - Database Initialization
-- Version: 1.0.0

CREATE DATABASE IF NOT EXISTS smart_survey
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_survey;

-- ============================================
-- 1. Users
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(200),
    tags JSON,
    real_name VARCHAR(50),
    id_card VARCHAR(255),
    is_verified TINYINT DEFAULT 0,
    level TINYINT DEFAULT 1,
    experience INT DEFAULT 0,
    reputation INT DEFAULT 100,
    balance DECIMAL(12,2) DEFAULT 0.00,
    frozen_balance DECIMAL(12,2) DEFAULT 0.00,
    role VARCHAR(20) DEFAULT 'user',
    status VARCHAR(20) DEFAULT 'normal',
    login_ip VARCHAR(45),
    login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_phone (phone),
    INDEX idx_users_email (email),
    INDEX idx_users_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. Surveys
-- ============================================
CREATE TABLE surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    status VARCHAR(20) DEFAULT 'draft',
    total_questions INT DEFAULT 0,
    total_responses INT DEFAULT 0,
    target_quota INT,
    remaining_quota INT,
    reward_type VARCHAR(20) DEFAULT 'fixed',
    reward_per_response DECIMAL(10,2),
    reward_total_budget DECIMAL(12,2),
    dispatch_type VARCHAR(20) DEFAULT 'public',
    target_audience JSON,
    is_anonymous TINYINT DEFAULT 0,
    allow_resume TINYINT DEFAULT 1,
    time_limit_minutes INT DEFAULT 0,
    max_attempts INT DEFAULT 1,
    start_time DATETIME,
    end_time DATETIME,
    closing_message VARCHAR(500) DEFAULT '感谢您的参与！',
    view_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    ai_generated TINYINT DEFAULT 0,
    audit_status VARCHAR(20) DEFAULT 'pending',
    audit_note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_surveys_user_status (user_id, status),
    INDEX idx_surveys_status_created (status, created_at),
    INDEX idx_surveys_marketplace (status, dispatch_type, remaining_quota),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';

-- ============================================
-- 3. Questions
-- ============================================
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'single/multiple/judge/fill/essay/rating/matrix/ranking',
    content TEXT NOT NULL,
    required TINYINT DEFAULT 1,
    order_index INT NOT NULL,
    options JSON COMMENT 'JSON array of option objects',
    settings JSON COMMENT 'Type-specific settings',
    logic_jump JSON COMMENT 'Jump logic rules',
    logic_show JSON COMMENT 'Show/hide conditions',
    is_random_options TINYINT DEFAULT 0,
    quota_limit JSON COMMENT 'Per-option quota limits',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_questions_survey_order (survey_id, order_index),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- ============================================
-- 4. Responses
-- ============================================
CREATE TABLE responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    device_fingerprint VARCHAR(64),
    user_agent VARCHAR(500),
    channel VARCHAR(50) COMMENT 'marketplace/share/direct',
    start_time DATETIME,
    end_time DATETIME,
    duration_seconds INT,
    reward_amount DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'in_progress' COMMENT 'in_progress/submitted/approved/rejected/expired',
    review_type VARCHAR(20) DEFAULT 'auto' COMMENT 'auto/manual',
    review_note TEXT,
    quality_score DECIMAL(3,2) COMMENT '0-10',
    behavior_data JSON COMMENT 'Per-question timing and interaction data',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_response_survey_user (survey_id, user_id),
    INDEX idx_responses_survey_status (survey_id, status),
    INDEX idx_responses_user_created (user_id, created_at),
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答记录表';

-- ============================================
-- 5. Answers
-- ============================================
CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    answer_options JSON COMMENT 'JSON array of selected option IDs',
    answer_rating INT,
    answer_order JSON COMMENT 'Ranking order results',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_answers_response (response_id),
    INDEX idx_answers_question (question_id),
    FOREIGN KEY (response_id) REFERENCES responses(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答案明细表';

-- ============================================
-- 6. Follows
-- ============================================
CREATE TABLE follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, followee_id),
    INDEX idx_follows_follower (follower_id),
    INDEX idx_follows_followee (followee_id),
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (followee_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- ============================================
-- 7. Messages
-- ============================================
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_messages_conversation (sender_id, receiver_id, created_at),
    INDEX idx_messages_receiver_unread (receiver_id, is_read),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- ============================================
-- 8. Transactions
-- ============================================
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'recharge/reward/withdraw/refund/freeze/unfreeze',
    amount DECIMAL(12,2) NOT NULL COMMENT 'positive=income, negative=expense',
    balance_before DECIMAL(12,2),
    balance_after DECIMAL(12,2),
    related_id BIGINT,
    related_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transactions_user_time (user_id, created_at),
    INDEX idx_transactions_type_time (type, created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- ============================================
-- 9. Notifications
-- ============================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'system/interaction/transaction/audit',
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id BIGINT,
    related_type VARCHAR(50),
    is_read TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_user_unread (user_id, is_read, created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================
-- 10. System Configs
-- ============================================
CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- Seed Data: Default system configurations
-- ============================================
INSERT INTO system_configs (config_key, config_value, description) VALUES
('platform.fee_rate', '0.10', '平台服务费率(10%%)'),
('platform.min_reward', '0.50', '最低每份奖励(元)'),
('platform.max_reward', '50.00', '最高每份奖励(元)'),
('withdraw.min_amount', '10.00', '最低提现金额(元)'),
('withdraw.max_daily', '5000.00', '单日最高提现(元)'),
('survey.max_questions', '100', '单问卷最大题目数'),
('claim.max_active', '5', '同时进行中的最大抢单数'),
('claim.expire_minutes', '15', '抢单过期时间(分钟)');
