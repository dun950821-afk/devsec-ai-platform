package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.PageResult;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.dto.DashboardVO;
import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.service.AuthService;
import com.guoshun.devsecai.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MainController {
    
    private final DashboardService dashboardService;
    private final AuthService authService;
    
    public MainController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }
    
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
    
    @GetMapping("/user/list")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(authService.getUserPage(current, size, keyword));
    }
    
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(authService.getUserById(id));
    }
    
    @PutMapping("/user/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        authService.updateUser(user);
        return Result.success("更新成功", null);
    }
    
    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        authService.updateStatus(id, status);
        return Result.success("更新成功", null);
    }
    
    @PostMapping("/user")
    public Result<Void> createUser(@RequestBody User user) {
        authService.register(user);
        return Result.success("创建成功", null);
    }
}
