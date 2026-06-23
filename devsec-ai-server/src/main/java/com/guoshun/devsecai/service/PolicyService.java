package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.ScanPolicy;
import com.guoshun.devsecai.mapper.ScanPolicyMapper;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {
    
    private final ScanPolicyMapper policyMapper;
    
    public PolicyService(ScanPolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }
    
    public Page<ScanPolicy> getPage(Integer current, Integer size, String keyword) {
        Page<ScanPolicy> page = new Page<>(current, size);
        LambdaQueryWrapper<ScanPolicy> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ScanPolicy::getName, keyword);
        }
        wrapper.orderByDesc(ScanPolicy::getCreateTime);
        return policyMapper.selectPage(page, wrapper);
    }
    
    public ScanPolicy getById(Long id) {
        return policyMapper.selectById(id);
    }
    
    public void create(ScanPolicy policy, Long userId) {
        policy.setUserId(userId);
        policyMapper.insert(policy);
    }
    
    public void update(ScanPolicy policy) {
        policyMapper.updateById(policy);
    }
    
    public void delete(Long id) {
        policyMapper.deleteById(id);
    }
    
    public void setDefault(Long id) {
        // 取消其他默认
        ScanPolicy current = new ScanPolicy();
        current.setIsDefault(0);
        policyMapper.update(current, new LambdaQueryWrapper<ScanPolicy>().eq(ScanPolicy::getIsDefault, 1));
        
        // 设置新默认
        ScanPolicy policy = new ScanPolicy();
        policy.setId(id);
        policy.setIsDefault(1);
        policyMapper.updateById(policy);
    }
}
