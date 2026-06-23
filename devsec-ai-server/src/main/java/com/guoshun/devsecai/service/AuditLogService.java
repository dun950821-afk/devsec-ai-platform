package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.AuditLog;
import com.guoshun.devsecai.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    
    private final AuditLogMapper auditLogMapper;
    
    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }
    
    public Page<AuditLog> getPage(Integer current, Integer size, String keyword, String operation, String startDate, String endDate) {
        Page<AuditLog> page = new Page<>(current, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AuditLog::getUsername, keyword).or().like(AuditLog::getDescription, keyword);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.eq(AuditLog::getOperation, operation);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AuditLog::getCreateTime, java.time.LocalDateTime.parse(startDate + " 00:00:00", java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AuditLog::getCreateTime, java.time.LocalDateTime.parse(endDate + " 23:59:59", java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(AuditLog::getCreateTime);
        return auditLogMapper.selectPage(page, wrapper);
    }
    
    public void saveLog(AuditLog log) {
        auditLogMapper.insert(log);
    }
}
