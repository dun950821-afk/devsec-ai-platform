package com.guoshun.devsecai.config;

import com.guoshun.devsecai.entity.*;
import com.guoshun.devsecai.mapper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataInitConfig {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PluginMapper pluginMapper;
    
    @Resource
    private ScanPolicyMapper policyMapper;
    
    @Resource
    private ScanRuleMapper ruleMapper;
    
    @Resource
    private AuditLogMapper auditLogMapper;
    
    @Resource
    private ProjectMapper projectMapper;
    
    @Resource
    private SkillMapper skillMapper;
    
    @Resource
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            initUsers();
            initPlugins();
            initProjects();
            initPolicies();
            initRules();
            initSkills();
            initAuditLogs();
            System.out.println("========== data init complete ==========");
        };
    }
    
    private void initUsers() {
        if (userMapper.selectCount(null) > 0) {
            System.out.println("users already exist, skip");
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("system admin");
        admin.setEmail("admin@devsecai.com");
        admin.setRole("admin");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        userMapper.insert(admin);
        
        User dev = new User();
        dev.setUsername("developer");
        dev.setPassword(passwordEncoder.encode("dev123"));
        dev.setRealName("developer");
        dev.setEmail("dev@devsecai.com");
        dev.setRole("developer");
        dev.setStatus(1);
        dev.setCreateTime(LocalDateTime.now());
        dev.setUpdateTime(LocalDateTime.now());
        userMapper.insert(dev);
        
        User op = new User();
        op.setUsername("operator");
        op.setPassword(passwordEncoder.encode("op123"));
        op.setRealName("operator");
        op.setEmail("op@devsecai.com");
        op.setRole("operator");
        op.setStatus(1);
        op.setCreateTime(LocalDateTime.now());
        op.setUpdateTime(LocalDateTime.now());
        userMapper.insert(op);
        
        System.out.println("init 3 users");
    }
    
    private void initPlugins() {
        if (pluginMapper.selectCount(null) > 0) {
            System.out.println("plugins already exist, skip");
            return;
        }
        
        Plugin p1 = new Plugin();
        p1.setName("IDEA Security Scanner");
        p1.setPluginKey("devsec-ai-idea");
        p1.setDescription("IntelliJ IDEA plugin for code security scanning, SAST, Secrets detection, and Git commit blocking");
        p1.setCategory("security");
        p1.setVersion("1.0.0");
        p1.setStatus(1);
        p1.setCreateTime(LocalDateTime.now());
        p1.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p1);
        
        Plugin p2 = new Plugin();
        p2.setName("Dependency Vulnerability Detector");
        p2.setPluginKey("dependency-vuln-detector");
        p2.setDescription("Detect known vulnerabilities in project dependencies");
        p2.setCategory("dependency");
        p2.setVersion("1.0.0");
        p2.setStatus(1);
        p2.setCreateTime(LocalDateTime.now());
        p2.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p2);
        
        Plugin p3 = new Plugin();
        p3.setName("Secret Credential Detector");
        p3.setPluginKey("secret-credential-detector");
        p3.setDescription("Detect hardcoded keys and credentials in source code");
        p3.setCategory("secret");
        p3.setVersion("1.0.0");
        p3.setStatus(1);
        p3.setCreateTime(LocalDateTime.now());
        p3.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p3);
        
        System.out.println("init 3 plugins");
    }
    
    private void initProjects() {
        if (projectMapper.selectCount(null) > 0) {
            System.out.println("projects already exist, skip");
            return;
        }
        
        Project proj1 = new Project();
        proj1.setName("DevSecAI Platform");
        proj1.setDescription("DevSecAI Plugin Management Platform - Web + Backend + IDEA Plugin");
        proj1.setStatus(1);
        proj1.setCreateTime(LocalDateTime.now());
        proj1.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(proj1);
        
        Project proj2 = new Project();
        proj2.setName("E-Commerce System");
        proj2.setDescription("Spring Boot e-commerce backend with payment integration");
        proj2.setStatus(1);
        proj2.setCreateTime(LocalDateTime.now());
        proj2.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(proj2);
        
        Project proj3 = new Project();
        proj3.setName("Mobile API Gateway");
        proj3.setDescription("API gateway for mobile applications with auth and rate limiting");
        proj3.setStatus(1);
        proj3.setCreateTime(LocalDateTime.now());
        proj3.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(proj3);
        
        System.out.println("init 3 projects");
    }
    
    private void initPolicies() {
        if (policyMapper.selectCount(null) > 0) {
            System.out.println("policies already exist, skip");
            return;
        }
        
        ScanPolicy policy = new ScanPolicy();
        policy.setName("Default Security Policy");
        policy.setDescription("Common security vulnerability detection rules");
        policy.setRuleIds("1,2,3");
        policy.setPluginIds("1,2,3");
        policy.setScanType(2);
        policy.setIsDefault(1);
        policy.setCreateTime(LocalDateTime.now());
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.insert(policy);
        
        ScanPolicy policy2 = new ScanPolicy();
        policy2.setName("Code Quality Policy");
        policy2.setDescription("Code quality and best practice checks");
        policy2.setRuleIds("1,2");
        policy2.setPluginIds("1");
        policy2.setScanType(1);
        policy2.setIsDefault(0);
        policy2.setCreateTime(LocalDateTime.now());
        policy2.setUpdateTime(LocalDateTime.now());
        policyMapper.insert(policy2);
        
        System.out.println("init 2 policies");
    }
    
    private void initRules() {
        if (ruleMapper.selectCount(null) > 0) {
            System.out.println("rules already exist, skip");
            return;
        }
        
        ScanRule rule1 = new ScanRule();
        rule1.setName("SQL Injection Detection");
        rule1.setRuleKey("sql-injection");
        rule1.setSeverity("high");
        rule1.setCategory("security");
        rule1.setDescription("Detect potential SQL injection vulnerabilities");
        rule1.setPattern("(?i)(select|insert|update|delete).*from.*where");
        rule1.setSolution("Use parameterized queries");
        rule1.setStatus(1);
        rule1.setPluginId(1L);
        rule1.setCreateTime(LocalDateTime.now());
        rule1.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule1);
        
        ScanRule rule2 = new ScanRule();
        rule2.setName("Hardcoded Password Detection");
        rule2.setRuleKey("hardcoded-password");
        rule2.setSeverity("high");
        rule2.setCategory("security");
        rule2.setDescription("Detect hardcoded passwords in source code");
        rule2.setPattern("(?i)(password|pwd|pass).*=.*[\"'][^\"']+[\"']");
        rule2.setSolution("Use environment variables or config files for sensitive data");
        rule2.setStatus(1);
        rule2.setPluginId(1L);
        rule2.setCreateTime(LocalDateTime.now());
        rule2.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule2);
        
        ScanRule rule3 = new ScanRule();
        rule3.setName("XSS Vulnerability Detection");
        rule3.setRuleKey("xss-vulnerability");
        rule3.setSeverity("medium");
        rule3.setCategory("security");
        rule3.setDescription("Detect cross-site scripting vulnerabilities");
        rule3.setPattern("(?i)innerHTML|document\\.write");
        rule3.setSolution("Use textContent instead of innerHTML");
        rule3.setStatus(1);
        rule3.setPluginId(1L);
        rule3.setCreateTime(LocalDateTime.now());
        rule3.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule3);
        
        System.out.println("init 3 rules");
    }
    
    private void initSkills() {
        if (skillMapper.selectCount(null) > 0) {
            System.out.println("skills already exist, skip");
            return;
        }
        
        Skill s1 = new Skill();
        s1.setSkillKey("ai-vuln-explain");
        s1.setName("Vulnerability Explanation");
        s1.setDescription("AI-powered vulnerability explanation and risk assessment");
        s1.setType("ai");
        s1.setStatus(1);
        s1.setCreateTime(LocalDateTime.now());
        s1.setUpdateTime(LocalDateTime.now());
        skillMapper.insert(s1);
        
        Skill s2 = new Skill();
        s2.setSkillKey("ai-fix-suggestion");
        s2.setName("Fix Suggestion");
        s2.setDescription("AI-powered code fix suggestions for security vulnerabilities");
        s2.setType("ai");
        s2.setStatus(1);
        s2.setCreateTime(LocalDateTime.now());
        s2.setUpdateTime(LocalDateTime.now());
        skillMapper.insert(s2);
        
        Skill s3 = new Skill();
        s3.setSkillKey("ai-patch-generate");
        s3.setName("Patch Generation");
        s3.setDescription("Automatically generate security patches for detected vulnerabilities");
        s3.setType("ai");
        s3.setStatus(1);
        s3.setCreateTime(LocalDateTime.now());
        s3.setUpdateTime(LocalDateTime.now());
        skillMapper.insert(s3);
        
        System.out.println("init 3 skills");
    }
    
    private void initAuditLogs() {
        if (auditLogMapper.selectCount(null) > 0) {
            System.out.println("audit logs already exist, skip");
            return;
        }
        
        AuditLog log1 = new AuditLog();
        log1.setUsername("admin");
        log1.setOperation("LOGIN");
        log1.setModule("Auth");
        log1.setStatus(1);
        log1.setIp("127.0.0.1");
        log1.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log1);
        
        AuditLog log2 = new AuditLog();
        log2.setUsername("admin");
        log2.setOperation("CREATE");
        log2.setModule("Plugin Management");
        log2.setDescription("Created IDEA Security Scanner plugin");
        log2.setStatus(1);
        log2.setIp("127.0.0.1");
        log2.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log2);
        
        System.out.println("init 2 audit logs");
    }
}
