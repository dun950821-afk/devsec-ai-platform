package com.guoshun.devsecai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class DatabaseInitRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有用户数据
        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SYS_USER", Integer.class);
            if (userCount != null && userCount > 0) {
                System.out.println("=== Database already initialized ===");
                return;
            }
        } catch (Exception e) {
            // 表不存在，需要初始化
        }

        System.out.println("=== Initializing database tables ===");
        
        // 创建用户表
        try {
            jdbcTemplate.execute("CREATE TABLE SYS_USER (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "USERNAME VARCHAR(50) NOT NULL UNIQUE, " +
                "PASSWORD VARCHAR(255) NOT NULL, " +
                "REAL_NAME VARCHAR(100), " +
                "EMAIL VARCHAR(100), " +
                "PHONE VARCHAR(20), " +
                "STATUS INT DEFAULT 1, " +
                "ROLE VARCHAR(20) DEFAULT 'user', " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建插件表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "PLUGIN_KEY VARCHAR(100) NOT NULL UNIQUE, " +
                "VERSION VARCHAR(50), " +
                "DESCRIPTION VARCHAR(500), " +
                "CATEGORY VARCHAR(50), " +
                "STATUS INT DEFAULT 0, " +
                "CONFIG VARCHAR(1000), " +
                "ENDPOINT VARCHAR(255), " +
                "LAST_HEARTBEAT TIMESTAMP, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建项目表
        try {
            jdbcTemplate.execute("CREATE TABLE PROJECT (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "CODE VARCHAR(100), " +
                "REPO_URL VARCHAR(500), " +
                "DESCRIPTION VARCHAR(500), " +
                "BRANCH VARCHAR(100), " +
                "USER_ID BIGINT, " +
                "SCAN_ENABLED INT DEFAULT 0, " +
                "POLICY_ID BIGINT, " +
                "STATUS INT DEFAULT 0, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建扫描策略表
        try {
            jdbcTemplate.execute("CREATE TABLE SCAN_POLICY (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "DESCRIPTION VARCHAR(500), " +
                "RULE_IDS VARCHAR(500), " +
                "PLUGIN_IDS VARCHAR(500), " +
                "SCAN_TYPE INT DEFAULT 0, " +
                "CRON_EXPRESSION VARCHAR(50), " +
                "USER_ID BIGINT, " +
                "IS_DEFAULT INT DEFAULT 0, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建扫描规则表
        try {
            jdbcTemplate.execute("CREATE TABLE SCAN_RULE (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "RULE_KEY VARCHAR(100) NOT NULL UNIQUE, " +
                "SEVERITY VARCHAR(20), " +
                "CATEGORY VARCHAR(50), " +
                "DESCRIPTION VARCHAR(500), " +
                "PATTERN VARCHAR(500), " +
                "SOLUTION VARCHAR(500), " +
                "STATUS INT DEFAULT 1, " +
                "PLUGIN_ID BIGINT, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建扫描结果表
        try {
            jdbcTemplate.execute("CREATE TABLE SCAN_RESULT (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "PROJECT_ID BIGINT, " +
                "POLICY_ID BIGINT, " +
                "BRANCH VARCHAR(100), " +
                "COMMIT_ID VARCHAR(100), " +
                "STATUS INT DEFAULT 0, " +
                "TOTAL_ISSUES INT DEFAULT 0, " +
                "CRITICAL_COUNT INT DEFAULT 0, " +
                "HIGH_COUNT INT DEFAULT 0, " +
                "MEDIUM_COUNT INT DEFAULT 0, " +
                "LOW_COUNT INT DEFAULT 0, " +
                "INFO_COUNT INT DEFAULT 0, " +
                "SUMMARY VARCHAR(2000), " +
                "START_TIME TIMESTAMP, " +
                "END_TIME TIMESTAMP, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建 Skill 表
        try {
            jdbcTemplate.execute("CREATE TABLE SKILL (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "SKILL_KEY VARCHAR(100) NOT NULL UNIQUE, " +
                "DESCRIPTION VARCHAR(500), " +
                "TYPE VARCHAR(50), " +
                "PROVIDER VARCHAR(50), " +
                "MODEL VARCHAR(100), " +
                "CONFIG VARCHAR(1000), " +
                "STATUS INT DEFAULT 1, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建审计日志表
        try {
            jdbcTemplate.execute("CREATE TABLE AUDIT_LOG (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "USER_ID BIGINT, " +
                "USERNAME VARCHAR(50), " +
                "MODULE VARCHAR(50), " +
                "OPERATION VARCHAR(50), " +
                "DESCRIPTION VARCHAR(500), " +
                "METHOD VARCHAR(10), " +
                "URL VARCHAR(500), " +
                "PARAM VARCHAR(2000), " +
                "IP VARCHAR(50), " +
                "STATUS INT DEFAULT 1, " +
                "ERROR_MSG VARCHAR(500), " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建插件能力表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN_CAPABILITY (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "PLUGIN_ID BIGINT NOT NULL, " +
                "CAPABILITY_KEY VARCHAR(100) NOT NULL, " +
                "CAPABILITY_NAME VARCHAR(100), " +
                "DESCRIPTION VARCHAR(500), " +
                "CONFIG VARCHAR(1000), " +
                "ENABLED INT DEFAULT 1, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 插入初始用户
        try {
            String adminPassword = passwordEncoder.encode("admin123");
            jdbcTemplate.update("INSERT INTO SYS_USER (USERNAME, PASSWORD, REAL_NAME, EMAIL, ROLE, STATUS) VALUES (?, ?, ?, ?, ?, ?)",
                "admin", adminPassword, "系统管理员", "admin@devsecai.com", "admin", 1);
            
            String userPassword = passwordEncoder.encode("user123");
            jdbcTemplate.update("INSERT INTO SYS_USER (USERNAME, PASSWORD, REAL_NAME, EMAIL, ROLE, STATUS) VALUES (?, ?, ?, ?, ?, ?)",
                "user", userPassword, "测试用户", "user@devsecai.com", "user", 1);
        } catch (Exception e) {
            System.err.println("User insertion warning: " + e.getMessage());
        }

        System.out.println("=== Database initialization completed ===");
        System.out.println("Admin: admin / admin123");
        System.out.println("User: user / user123");
    }
}
