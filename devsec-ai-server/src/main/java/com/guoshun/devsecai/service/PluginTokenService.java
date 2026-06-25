package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.PluginToken;
import com.guoshun.devsecai.mapper.PluginTokenMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PluginTokenService {

    private final PluginTokenMapper tokenMapper;

    public PluginTokenService(PluginTokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    public Page<PluginToken> getPage(Integer current, Integer size, String keyword, String status) {
        Page<PluginToken> page = new Page<>(current, size);
        LambdaQueryWrapper<PluginToken> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(PluginToken::getName, keyword).or().like(PluginToken::getToken, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PluginToken::getStatus, status);
        }
        wrapper.orderByDesc(PluginToken::getCreateTime);
        return tokenMapper.selectPage(page, wrapper);
    }

    public PluginToken getById(Long id) {
        return tokenMapper.selectById(id);
    }

    public PluginToken create(String name, Long userId, String expireTimeStr) {
        PluginToken token = new PluginToken();
        token.setToken("plt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        token.setName(name);
        token.setUserId(userId);
        token.setStatus("ACTIVE");

        // 解析过期时间
        if (expireTimeStr != null && !expireTimeStr.isEmpty() && !"never".equals(expireTimeStr)) {
            LocalDateTime now = LocalDateTime.now();
            switch (expireTimeStr) {
                case "30d": token.setExpireTime(now.plusDays(30)); break;
                case "90d": token.setExpireTime(now.plusDays(90)); break;
                case "180d": token.setExpireTime(now.plusDays(180)); break;
                case "1y": token.setExpireTime(now.plusYears(1)); break;
                default: token.setExpireTime(now.plusYears(1));
            }
        }

        tokenMapper.insert(token);
        return token;
    }

    public void revoke(Long id) {
        PluginToken token = tokenMapper.selectById(id);
        if (token == null) {
            throw new RuntimeException("Token不存在");
        }
        token.setStatus("REVOKED");
        tokenMapper.updateById(token);
    }
}
