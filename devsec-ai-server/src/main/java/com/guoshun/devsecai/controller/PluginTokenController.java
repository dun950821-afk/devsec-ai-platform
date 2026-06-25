package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.PluginToken;
import com.guoshun.devsecai.service.PluginTokenService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plugin-token")
public class PluginTokenController {

    private final PluginTokenService tokenService;

    public PluginTokenController(PluginTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/list")
    public Result<Page<PluginToken>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(tokenService.getPage(current, size, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<PluginToken> getById(@PathVariable Long id) {
        return Result.success(tokenService.getById(id));
    }

    @PostMapping
    public Result<PluginToken> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String expireTime = body.get("expireTime");
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId")) : 1L;
        if (name == null || name.isEmpty()) {
            return Result.error("Token名称不能为空");
        }
        PluginToken token = tokenService.create(name, userId, expireTime);
        return Result.success("生成成功", token);
    }

    @PutMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        tokenService.revoke(id);
        return Result.success("吊销成功", null);
    }
}
