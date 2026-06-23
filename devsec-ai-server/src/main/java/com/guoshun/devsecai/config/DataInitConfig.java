package com.guoshun.devsecai.config;

import com.guoshun.devsecai.entity.*;
import com.guoshun.devsecai.mapper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;

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
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            initUsers();
            initPlugins();
            initPolicies();
            initRules();
            initAuditLogs();
            System.out.println("========== 数据初始化完成 ==========");
        };
    }
    
    private void initUsers() {
        if (userMapper.selectCount(null) > 0) {
            System.out.println("用户数据已存在，跳过初始化");
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setEmail("admin@devsecai.com");
        admin.setRole("admin");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        userMapper.insert(admin);
        
        User dev = new User();
        dev.setUsername("developer");
        dev.setPassword(passwordEncoder.encode("dev123"));
        dev.setRealName("开发人员");
        dev.setEmail("dev@devsecai.com");
        dev.setRole("developer");
        dev.setStatus(1);
        dev.setCreateTime(LocalDateTime.now());
        dev.setUpdateTime(LocalDateTime.now());
        userMapper.insert(dev);
        
        User op = new User();
        op.setUsername("operator");
        op.setPassword(passwordEncoder.encode("op123"));
        op.setRealName("运维人员");
        op.setEmail("op@devsecai.com");
        op.setRole("operator");
        op.setStatus(1);
        op.setCreateTime(LocalDateTime.now());
        op.setUpdateTime(LocalDateTime.now());
        userMapper.insert(op);
        
        System.out.println("初始化了3个用户");
    }
    
    private void initPlugins() {
        if (pluginMapper.selectCount(null) > 0) {
            System.out.println("插件数据已存在，跳过初始化");
            return;
        }
        
        Plugin p1 = new Plugin();
        p1.setName("代码安全扫描插件");
        p1.setDescription("扫描源代码中的安全漏洞和潜在风险");
        p1.setCategory("security");
        p1.setVersion("1.0.0");
        p1.setStatus(1);
        p1.setCreateTime(LocalDateTime.now());
        p1.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p1);
        
        Plugin p2 = new Plugin();
        p2.setName("依赖漏洞检测插件");
        p2.setDescription("检测项目依赖中的已知漏洞");
        p2.setCategory("dependency");
        p2.setVersion("1.0.0");
        p2.setStatus(1);
        p2.setCreateTime(LocalDateTime.now());
        p2.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p2);
        
        Plugin p3 = new Plugin();
        p3.setName("秘钥凭证检测插件");
        p3.setDescription("检测代码中的硬编码密钥和凭证");
        p3.setCategory("secret");
        p3.setVersion("1.0.0");
        p3.setStatus(1);
        p3.setCreateTime(LocalDateTime.now());
        p3.setUpdateTime(LocalDateTime.now());
        pluginMapper.insert(p3);
        
        System.out.println("初始化了3个插件");
    }
    
    private void initPolicies() {
        if (policyMapper.selectCount(null) > 0) {
            System.out.println("策略数据已存在，跳过初始化");
            return;
        }
        
        ScanPolicy policy = new ScanPolicy();
        policy.setName("默认安全扫描策略");
        policy.setDescription("包含常见安全漏洞检测规则");
        policy.setRuleIds("1,2,3");
        policy.setPluginIds("1,2,3");
        policy.setScanType(2);
        policy.setIsDefault(1);
        policy.setCreateTime(LocalDateTime.now());
        policy.setUpdateTime(LocalDateTime.now());
        policyMapper.insert(policy);
        
        ScanPolicy policy2 = new ScanPolicy();
        policy2.setName("代码质量策略");
        policy2.setDescription("代码质量和最佳实践检查");
        policy2.setRuleIds("1,2");
        policy2.setPluginIds("1");
        policy2.setScanType(1);
        policy2.setIsDefault(0);
        policy2.setCreateTime(LocalDateTime.now());
        policy2.setUpdateTime(LocalDateTime.now());
        policyMapper.insert(policy2);
        
        System.out.println("初始化了2个策略");
    }
    
    private void initRules() {
        if (ruleMapper.selectCount(null) > 0) {
            System.out.println("规则数据已存在，跳过初始化");
            return;
        }
        
        ScanRule rule1 = new ScanRule();
        rule1.setName("SQL注入检测");
        rule1.setRuleKey("sql-injection");
        rule1.setSeverity("high");
        rule1.setCategory("security");
        rule1.setDescription("检测可能的SQL注入漏洞");
        rule1.setPattern("(?i)(select|insert|update|delete).*from.*where");
        rule1.setSolution("使用参数化查询");
        rule1.setStatus(1);
        rule1.setPluginId(1L);
        rule1.setCreateTime(LocalDateTime.now());
        rule1.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule1);
        
        ScanRule rule2 = new ScanRule();
        rule2.setName("硬编码密码检测");
        rule2.setRuleKey("hardcoded-password");
        rule2.setSeverity("high");
        rule2.setCategory("security");
        rule2.setDescription("检测代码中的硬编码密码");
        rule2.setPattern("(?i)(password|pwd|pass).*=.*[\"'][^\"']+[\"']");
        rule2.setSolution("使用环境变量或配置文件存储敏感信息");
        rule2.setStatus(1);
        rule2.setPluginId(1L);
        rule2.setCreateTime(LocalDateTime.now());
        rule2.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule2);
        
        ScanRule rule3 = new ScanRule();
        rule3.setName("XSS漏洞检测");
        rule3.setRuleKey("xss-vulnerability");
        rule3.setSeverity("medium");
        rule3.setCategory("security");
        rule3.setDescription("检测跨站脚本攻击漏洞");
        rule3.setPattern("(?i)innerHTML|document\\.write");
        rule3.setSolution("使用textContent代替innerHTML");
        rule3.setStatus(1);
        rule3.setPluginId(1L);
        rule3.setCreateTime(LocalDateTime.now());
        rule3.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule3);
        
        System.out.println("初始化了3条规则");
    }
    
    private void initAuditLogs() {
        if (auditLogMapper.selectCount(null) > 0) {
            System.out.println("审计日志数据已存在，跳过初始化");
            return;
        }
        
        AuditLog log1 = new AuditLog();
        log1.setUsername("admin");
        log1.setOperation("LOGIN");
        log1.setModule("认证");
        log1.setStatus(1);
        log1.setIp("127.0.0.1");
        log1.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log1);
        
        AuditLog log2 = new AuditLog();
        log2.setUsername("admin");
        log2.setOperation("CREATE");
        log2.setModule("插件管理");
        log2.setDescription("创建了代码安全扫描插件");
        log2.setStatus(1);
        log2.setIp("127.0.0.1");
        log2.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log2);
        
        System.out.println("初始化了2条审计日志");
    }
}
