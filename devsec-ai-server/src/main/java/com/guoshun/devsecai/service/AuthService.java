package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.PageResult;
import com.guoshun.devsecai.dto.LoginVO;
import com.guoshun.devsecai.entity.AuditLog;
import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.mapper.AuditLogMapper;
import com.guoshun.devsecai.mapper.UserMapper;
import com.guoshun.devsecai.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuditLogMapper auditLogMapper;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                      JwtTokenUtil jwtTokenUtil, AuditLogMapper auditLogMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.auditLogMapper = auditLogMapper;
    }
    
    public LoginVO login(String username, String password, String ip) {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            saveAuditLog(null, username, "LOGIN", "用户登录", ip, 0, "用户不存在");
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            saveAuditLog(user.getId(), username, "LOGIN", "用户登录", ip, 0, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            saveAuditLog(user.getId(), username, "LOGIN", "用户登录", ip, 0, "账号已禁用");
            throw new RuntimeException("账号已被禁用");
        }
        
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        saveAuditLog(user.getId(), username, "LOGIN", "用户登录成功", ip, 1, null);
        
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpireTime(System.currentTimeMillis() + expiration);
        
        LoginVO.UserInfoVO userInfo = new LoginVO.UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(user.getRole());
        vo.setUserInfo(userInfo);
        
        return vo;
    }
    
    public void logout(Long userId, String username, String ip) {
        if (userId != null) {
            saveAuditLog(userId, username, "LOGOUT", "用户退出登录", ip, 1, null);
        }
    }
    
    public PageResult<User> getUserPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getRealName, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        
        Page<User> result = userMapper.selectPage(page, wrapper);
        
        List<User> records = result.getRecords().stream()
            .peek(user -> user.setPassword(null))
            .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), records, result.getCurrent(), result.getSize());
    }
    
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
    
    public void createUser(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }
    
    public void updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }
    
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }
    
    public void updateUserStatus(Long id, Integer status) {
        updateStatus(id, status);
    }
    
    public User register(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }
    
    private void saveAuditLog(Long userId, String username, String module, String operation, String ip, Integer status, String description) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setDescription(description);
        log.setIp(ip);
        log.setStatus(status);
        log.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
