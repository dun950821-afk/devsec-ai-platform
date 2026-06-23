package com.guoshun.devsecai.controller;

import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.dto.LoginDTO;
import com.guoshun.devsecai.dto.LoginVO;
import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO vo = authService.login(dto.getUsername(), dto.getPassword(), ip);
        return Result.success(vo);
    }
    
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String ip = getClientIp(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            authService.logout(user.getId(), user.getUsername(), ip);
        }
        return Result.success("退出成功", null);
    }
    
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            return Result.error(401, "未登录");
        }
        User user = (User) auth.getPrincipal();
        return Result.success(user);
    }
    
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        authService.register(user);
        return Result.success("注册成功", null);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
