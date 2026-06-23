-- 国舜 DevSecAI 插件管理平台 MySQL 兼容建表脚本

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    role VARCHAR(20) DEFAULT 'user',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS plugin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    plugin_key VARCHAR(100) NOT NULL UNIQUE,
    version VARCHAR(50),
    description VARCHAR(500),
    category VARCHAR(50),
    status INT DEFAULT 0,
    config VARCHAR(1000),
    endpoint VARCHAR(255),
    last_heartbeat TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100),
    repo_url VARCHAR(500),
    description VARCHAR(500),
    branch VARCHAR(100),
    user_id BIGINT,
    scan_enabled INT DEFAULT 0,
    policy_id BIGINT,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS scan_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    rule_ids VARCHAR(500),
    plugin_ids VARCHAR(500),
    scan_type INT DEFAULT 0,
    cron_expression VARCHAR(50),
    user_id BIGINT,
    is_default INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS scan_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rule_key VARCHAR(100) NOT NULL UNIQUE,
    severity VARCHAR(20),
    category VARCHAR(50),
    description VARCHAR(500),
    pattern VARCHAR(500),
    solution VARCHAR(500),
    status INT DEFAULT 1,
    plugin_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS scan_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    policy_id BIGINT,
    branch VARCHAR(100),
    commit_id VARCHAR(100),
    status INT DEFAULT 0,
    total_issues INT DEFAULT 0,
    critical_count INT DEFAULT 0,
    high_count INT DEFAULT 0,
    medium_count INT DEFAULT 0,
    low_count INT DEFAULT 0,
    info_count INT DEFAULT 0,
    summary VARCHAR(2000),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    skill_key VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    type VARCHAR(50),
    provider VARCHAR(50),
    model VARCHAR(100),
    config VARCHAR(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    module VARCHAR(50),
    operation VARCHAR(50),
    description VARCHAR(500),
    method VARCHAR(10),
    url VARCHAR(500),
    param VARCHAR(2000),
    ip VARCHAR(50),
    status INT DEFAULT 1,
    error_msg VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS plugin_capability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plugin_id BIGINT NOT NULL,
    capability_key VARCHAR(100) NOT NULL,
    capability_name VARCHAR(100),
    description VARCHAR(500),
    config VARCHAR(1000),
    enabled INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 初始化管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, email, role, status)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@devsecai.com', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (username, password, real_name, email, role, status)
SELECT 'user', '$2a$10$X5wFutsrWd86F2K2hLrKUOQJ9W3p5c3b5p0rK2m8n5t4Q1R2S3T4U', '测试用户', 'user@devsecai.com', 'user', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'user');
