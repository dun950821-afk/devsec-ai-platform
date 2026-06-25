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
        // Check if user data already exists (for data seeding skip logic)
        boolean hasExistingData = false;
        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SYS_USER", Integer.class);
            if (userCount != null && userCount > 0) {
                hasExistingData = true;
            }
        } catch (Exception e) {
            // Table doesn't exist, need to initialize
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

        // 创建插件实例表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN_INSTANCE (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "PLUGIN_ID VARCHAR(50) NOT NULL UNIQUE, " +
                "PLUGIN_VERSION VARCHAR(50), " +
                "IDE_NAME VARCHAR(100), " +
                "IDE_VERSION VARCHAR(50), " +
                "DEVELOPER VARCHAR(100), " +
                "MACHINE_ID VARCHAR(255), " +
                "PROJECT_ID BIGINT, " +
                "PROJECT_NAME VARCHAR(100), " +
                "POLICY_ID BIGINT, " +
                "POLICY_NAME VARCHAR(100), " +
                "STATUS VARCHAR(30) DEFAULT 'OFFLINE', " +
                "LAST_HEARTBEAT TIMESTAMP, " +
                "ACCESS_TOKEN VARCHAR(100), " +
                "ENABLED_MODULES VARCHAR(500), " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建插件能力策略表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN_CAPABILITY_POLICY (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "POLICY_NAME VARCHAR(100) NOT NULL, " +
                "POLICY_VERSION VARCHAR(50), " +
                "SCA_ENABLED BOOLEAN DEFAULT TRUE, " +
                "SAST_ENABLED BOOLEAN DEFAULT TRUE, " +
                "SECRETS_ENABLED BOOLEAN DEFAULT TRUE, " +
                "BASELINE_ENABLED BOOLEAN DEFAULT FALSE, " +
                "AI_ENABLED BOOLEAN DEFAULT FALSE, " +
                "AI_VULN_EXPLAIN BOOLEAN DEFAULT FALSE, " +
                "AI_FIX_SUGGESTION BOOLEAN DEFAULT FALSE, " +
                "AI_PATCH_GENERATE BOOLEAN DEFAULT FALSE, " +
                "GIT_COMMIT_CHECK BOOLEAN DEFAULT TRUE, " +
                "BLOCK_CRITICAL BOOLEAN DEFAULT TRUE, " +
                "BLOCK_HIGH BOOLEAN DEFAULT TRUE, " +
                "BLOCK_MEDIUM BOOLEAN DEFAULT FALSE, " +
                "BLOCK_LOW BOOLEAN DEFAULT FALSE, " +
                "AUTO_UPLOAD BOOLEAN DEFAULT TRUE, " +
                "ALLOW_CODE_SNIPPET BOOLEAN DEFAULT TRUE, " +
                "MASK_SECRETS BOOLEAN DEFAULT TRUE, " +
                "MAX_CONTEXT_LINES INT DEFAULT 80, " +
                "PROJECT_ID BIGINT, " +
                "STATUS INT DEFAULT 1, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建插件Token表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN_TOKEN (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "TOKEN VARCHAR(100) NOT NULL UNIQUE, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "USER_ID BIGINT, " +
                "BIND_PLUGIN_ID VARCHAR(50), " +
                "STATUS VARCHAR(20) DEFAULT 'ACTIVE', " +
                "EXPIRE_TIME TIMESTAMP, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UPDATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 创建插件Finding表
        try {
            jdbcTemplate.execute("CREATE TABLE PLUGIN_FINDING (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "PLUGIN_ID VARCHAR(50), " +
                "MODULE VARCHAR(30), " +
                "RULE_ID VARCHAR(100), " +
                "SEVERITY VARCHAR(20), " +
                "TITLE VARCHAR(500), " +
                "DESCRIPTION VARCHAR(2000), " +
                "FILE_PATH VARCHAR(500), " +
                "START_LINE INT, " +
                "END_LINE INT, " +
                "COMPONENT_NAME VARCHAR(200), " +
                "CURRENT_VERSION VARCHAR(50), " +
                "FIXED_VERSION VARCHAR(50), " +
                "VULNERABILITY_ID VARCHAR(100), " +
                "CONFIDENCE VARCHAR(20), " +
                "RECOMMENDATION VARCHAR(1000), " +
                "CWE VARCHAR(50), " +
                "OWASP VARCHAR(100), " +
                "STATUS VARCHAR(20) DEFAULT 'OPEN', " +
                "PROJECT_ID BIGINT, " +
                "CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "DELETED INT DEFAULT 0)");
        } catch (Exception e) {
            System.err.println("Table creation warning: " + e.getMessage());
        }

        // 插入初始数据（仅在首次初始化时）
        System.out.println("=== hasExistingData = " + hasExistingData + " ===");
        if (!hasExistingData) {
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

        // 插入示例能力策略
        try {
            jdbcTemplate.update("INSERT INTO PLUGIN_CAPABILITY_POLICY " +
                "(POLICY_NAME, POLICY_VERSION, SCA_ENABLED, SAST_ENABLED, SECRETS_ENABLED, BASELINE_ENABLED, " +
                "AI_ENABLED, AI_VULN_EXPLAIN, AI_FIX_SUGGESTION, AI_PATCH_GENERATE, " +
                "GIT_COMMIT_CHECK, BLOCK_CRITICAL, BLOCK_HIGH, BLOCK_MEDIUM, BLOCK_LOW, " +
                "AUTO_UPLOAD, ALLOW_CODE_SNIPPET, MASK_SECRETS, MAX_CONTEXT_LINES, PROJECT_ID, STATUS) " +
                "VALUES ('银行Java安全策略', '1.0.3', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, TRUE, 80, 1, 1)");
            jdbcTemplate.update("INSERT INTO PLUGIN_CAPABILITY_POLICY " +
                "(POLICY_NAME, POLICY_VERSION, SCA_ENABLED, SAST_ENABLED, SECRETS_ENABLED, BASELINE_ENABLED, " +
                "AI_ENABLED, AI_VULN_EXPLAIN, AI_FIX_SUGGESTION, AI_PATCH_GENERATE, " +
                "GIT_COMMIT_CHECK, BLOCK_CRITICAL, BLOCK_HIGH, BLOCK_MEDIUM, BLOCK_LOW, " +
                "AUTO_UPLOAD, ALLOW_CODE_SNIPPET, MASK_SECRETS, MAX_CONTEXT_LINES, PROJECT_ID, STATUS) " +
                "VALUES ('通用开发策略', '1.0.1', TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, TRUE, TRUE, 60, 2, 1)");
            jdbcTemplate.update("INSERT INTO PLUGIN_CAPABILITY_POLICY " +
                "(POLICY_NAME, POLICY_VERSION, SCA_ENABLED, SAST_ENABLED, SECRETS_ENABLED, BASELINE_ENABLED, " +
                "AI_ENABLED, AI_VULN_EXPLAIN, AI_FIX_SUGGESTION, AI_PATCH_GENERATE, " +
                "GIT_COMMIT_CHECK, BLOCK_CRITICAL, BLOCK_HIGH, BLOCK_MEDIUM, BLOCK_LOW, " +
                "AUTO_UPLOAD, ALLOW_CODE_SNIPPET, MASK_SECRETS, MAX_CONTEXT_LINES, STATUS) " +
                "VALUES ('轻量检测策略', '0.9.0', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 40, 0)");
        } catch (Exception e) {
            System.err.println("Capability policy insertion warning: " + e.getMessage());
        }

        // 插入示例Token
        try {
            jdbcTemplate.update("INSERT INTO PLUGIN_TOKEN (TOKEN, NAME, USER_ID, BIND_PLUGIN_ID, STATUS, EXPIRE_TIME) " +
                "VALUES ('plt_a8f3c2d1e5b7d912', '支付团队Token', 1, 'IDEA-PLG-000128', 'ACTIVE', '2027-06-25 00:00:00')");
            jdbcTemplate.update("INSERT INTO PLUGIN_TOKEN (TOKEN, NAME, USER_ID, BIND_PLUGIN_ID, STATUS, EXPIRE_TIME) " +
                "VALUES ('plt_b2e1f4a8c6d34c7f', '开发组Token', 1, 'IDEA-PLG-000256', 'ACTIVE', '2027-03-15 00:00:00')");
            jdbcTemplate.update("INSERT INTO PLUGIN_TOKEN (TOKEN, NAME, USER_ID, BIND_PLUGIN_ID, STATUS, EXPIRE_TIME) " +
                "VALUES ('plt_c5d9e2b7a1f68a3e', '测试Token', 2, NULL, 'EXPIRED', '2026-01-01 00:00:00')");
            jdbcTemplate.update("INSERT INTO PLUGIN_TOKEN (TOKEN, NAME, USER_ID, BIND_PLUGIN_ID, STATUS, EXPIRE_TIME) " +
                "VALUES ('plt_f1a7b3c5d9e82b5c', '旧版Token', 1, 'IDEA-PLG-000384', 'REVOKED', '2026-12-01 00:00:00')");
        } catch (Exception e) {
            System.err.println("Token insertion warning: " + e.getMessage());
        }

        // 插入示例插件实例
        try {
            jdbcTemplate.update("INSERT INTO PLUGIN_INSTANCE " +
                "(PLUGIN_ID, PLUGIN_VERSION, IDE_NAME, IDE_VERSION, DEVELOPER, MACHINE_ID, PROJECT_ID, PROJECT_NAME, " +
                "POLICY_ID, POLICY_NAME, STATUS, LAST_HEARTBEAT, ACCESS_TOKEN, ENABLED_MODULES) " +
                "VALUES ('IDEA-PLG-000128', '0.9.6', 'IntelliJ IDEA', '2025.2', '张三', 'hash-machine-001', 1, '支付核心系统', " +
                "1, '银行Java安全策略', 'ONLINE', '2026-06-25 10:30:00', 'plt_a8f3c2d1e5b7d912', 'SCA,SAST,SECRETS,BASELINE,AI')");
            jdbcTemplate.update("INSERT INTO PLUGIN_INSTANCE " +
                "(PLUGIN_ID, PLUGIN_VERSION, IDE_NAME, IDE_VERSION, DEVELOPER, MACHINE_ID, PROJECT_ID, PROJECT_NAME, " +
                "POLICY_ID, POLICY_NAME, STATUS, LAST_HEARTBEAT, ACCESS_TOKEN, ENABLED_MODULES) " +
                "VALUES ('IDEA-PLG-000256', '0.9.5', 'IntelliJ IDEA', '2025.1', '李四', 'hash-machine-002', 2, '用户中心', " +
                "2, '通用开发策略', 'ONLINE', '2026-06-25 10:25:00', 'plt_b2e1f4a8c6d34c7f', 'SCA,SAST,AI')");
            jdbcTemplate.update("INSERT INTO PLUGIN_INSTANCE " +
                "(PLUGIN_ID, PLUGIN_VERSION, IDE_NAME, IDE_VERSION, DEVELOPER, MACHINE_ID, PROJECT_ID, PROJECT_NAME, " +
                "POLICY_ID, POLICY_NAME, STATUS, LAST_HEARTBEAT, ACCESS_TOKEN, ENABLED_MODULES) " +
                "VALUES ('IDEA-PLG-000384', '0.9.4', 'WebStorm', '2025.1', '王五', 'hash-machine-003', 3, '风控引擎', " +
                "1, '银行Java安全策略', 'OFFLINE', '2026-06-24 18:00:00', 'plt_f1a7b3c5d9e82b5c', 'SCA,SAST')");
            jdbcTemplate.update("INSERT INTO PLUGIN_INSTANCE " +
                "(PLUGIN_ID, PLUGIN_VERSION, IDE_NAME, IDE_VERSION, DEVELOPER, MACHINE_ID, PROJECT_ID, PROJECT_NAME, " +
                "POLICY_ID, POLICY_NAME, STATUS, LAST_HEARTBEAT, ACCESS_TOKEN, ENABLED_MODULES) " +
                "VALUES ('IDEA-PLG-000512', '0.9.2', 'IntelliJ IDEA', '2024.3', '赵六', 'hash-machine-004', 4, '数据平台', " +
                "3, '轻量检测策略', 'ERROR', '2026-06-23 09:15:00', NULL, 'SCA')");
            jdbcTemplate.update("INSERT INTO PLUGIN_INSTANCE " +
                "(PLUGIN_ID, PLUGIN_VERSION, IDE_NAME, IDE_VERSION, DEVELOPER, MACHINE_ID, PROJECT_ID, PROJECT_NAME, " +
                "POLICY_ID, POLICY_NAME, STATUS, LAST_HEARTBEAT, ACCESS_TOKEN, ENABLED_MODULES) " +
                "VALUES ('IDEA-PLG-000640', '0.9.6', 'IntelliJ IDEA', '2025.2', '孙七', 'hash-machine-005', 5, 'API网关', " +
                "2, '通用开发策略', 'UPGRADE_REQUIRED', '2026-06-25 08:00:00', NULL, 'SCA,SAST,AI')");
        } catch (Exception e) {
            System.err.println("Plugin instance insertion warning: " + e.getMessage());
        }

        // 插入示例Finding数据
        try {
            jdbcTemplate.update("INSERT INTO PLUGIN_FINDING " +
                "(PLUGIN_ID, MODULE, RULE_ID, SEVERITY, TITLE, DESCRIPTION, FILE_PATH, START_LINE, END_LINE, " +
                "COMPONENT_NAME, CURRENT_VERSION, FIXED_VERSION, VULNERABILITY_ID, STATUS, PROJECT_ID) " +
                "VALUES ('IDEA-PLG-000128', 'SCA', 'SCA-MAVEN-LOG4J-CVE-2021-44228', 'CRITICAL', " +
                "'Log4j2 远程代码执行漏洞', '检测到使用受影响版本的log4j-core', 'pom.xml', NULL, NULL, " +
                "'org.apache.logging.log4j:log4j-core', '2.14.1', '2.17.1', 'CVE-2021-44228', 'OPEN', 1)");
            jdbcTemplate.update("INSERT INTO PLUGIN_FINDING " +
                "(PLUGIN_ID, MODULE, RULE_ID, SEVERITY, TITLE, DESCRIPTION, FILE_PATH, START_LINE, END_LINE, " +
                "CONFIDENCE, RECOMMENDATION, CWE, OWASP, STATUS, PROJECT_ID) " +
                "VALUES ('IDEA-PLG-000128', 'SAST', 'JAVA-SQL-INJECTION-001', 'HIGH', " +
                "'疑似SQL注入风险', '检测到MyBatis使用${}拼接外部参数', " +
                "'src/main/java/com/example/UserMapper.java', 42, 44, " +
                "'MEDIUM', '建议使用#{}参数绑定', 'CWE-89', 'A03:2021-Injection', 'OPEN', 1)");
            jdbcTemplate.update("INSERT INTO PLUGIN_FINDING " +
                "(PLUGIN_ID, MODULE, RULE_ID, SEVERITY, TITLE, DESCRIPTION, FILE_PATH, START_LINE, END_LINE, " +
                "CONFIDENCE, RECOMMENDATION, CWE, OWASP, STATUS, PROJECT_ID) " +
                "VALUES ('IDEA-PLG-000256', 'SAST', 'JAVA-XSS-001', 'MEDIUM', " +
                "'XSS跨站脚本风险', '用户输入未经过滤直接输出到页面', " +
                "'src/main/java/com/example/UserController.java', 88, 90, " +
                "'HIGH', '建议对用户输入进行HTML转义', 'CWE-79', 'A03:2021-Injection', 'OPEN', 2)");
            jdbcTemplate.update("INSERT INTO PLUGIN_FINDING " +
                "(PLUGIN_ID, MODULE, RULE_ID, SEVERITY, TITLE, DESCRIPTION, FILE_PATH, START_LINE, END_LINE, " +
                "COMPONENT_NAME, CURRENT_VERSION, FIXED_VERSION, VULNERABILITY_ID, STATUS, PROJECT_ID) " +
                "VALUES ('IDEA-PLG-000256', 'SCA', 'SCA-NPM-LODASH-PROTOTYPE-POLLUTION', 'LOW', " +
                "'Lodash 原型污染漏洞', '检测到使用受影响版本的lodash', 'package.json', NULL, NULL, " +
                "'lodash', '4.17.20', '4.17.21', 'CVE-2021-23337', 'FIXED', 2)");
        } catch (Exception e) {
            System.err.println("Finding insertion warning: " + e.getMessage());
        }
        } // end if (!hasExistingData)

        System.out.println("=== Database initialization completed ===");
        System.out.println("Admin: admin / admin123");
        System.out.println("User: user / user123");
    }
}
