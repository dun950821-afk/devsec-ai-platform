package com.guoshun.devsecai.config;

import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitConfig {

    @Bean
    CommandLineRunner initDatabase(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        return args -> {
            // 检查是否已有用户数据
            if (userMapper.selectCount(null) == 0) {
                // 初始化管理员用户
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setEmail("admin@devsecai.com");
                admin.setRole("admin");
                admin.setStatus(1);
                userMapper.insert(admin);

                // 初始化普通用户
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRealName("测试用户");
                user.setEmail("user@devsecai.com");
                user.setRole("user");
                user.setStatus(1);
                userMapper.insert(user);
                
                System.out.println("=== Initial user data created ===");
                System.out.println("Admin: admin / admin123");
                System.out.println("User: user / user123");
            }
        };
    }
}
