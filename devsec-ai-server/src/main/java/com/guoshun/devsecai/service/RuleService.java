package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.ScanRule;
import com.guoshun.devsecai.mapper.ScanRuleMapper;
import org.springframework.stereotype.Service;

@Service
public class RuleService {
    
    private final ScanRuleMapper ruleMapper;
    
    public RuleService(ScanRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }
    
    public Page<ScanRule> getPage(Integer current, Integer size, String keyword, String severity, String category) {
        Page<ScanRule> page = new Page<>(current, size);
        LambdaQueryWrapper<ScanRule> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ScanRule::getName, keyword).or().like(ScanRule::getRuleKey, keyword);
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(ScanRule::getSeverity, severity);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ScanRule::getCategory, category);
        }
        wrapper.orderByDesc(ScanRule::getCreateTime);
        return ruleMapper.selectPage(page, wrapper);
    }
    
    public ScanRule getById(Long id) {
        return ruleMapper.selectById(id);
    }
    
    public void create(ScanRule rule) {
        ruleMapper.insert(rule);
    }
    
    public void update(ScanRule rule) {
        ruleMapper.updateById(rule);
    }
    
    public void delete(Long id) {
        ruleMapper.deleteById(id);
    }
    
    public void updateStatus(Long id, Integer status) {
        ScanRule rule = new ScanRule();
        rule.setId(id);
        rule.setStatus(status);
        ruleMapper.updateById(rule);
    }
}
