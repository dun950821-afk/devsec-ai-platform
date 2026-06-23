package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.AuditLog;
import com.guoshun.devsecai.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    
    private final AuditLogService auditLogService;
    
    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @GetMapping("/list")
    public Result<Page<AuditLog>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(auditLogService.getPage(current, size, keyword, operation, startDate, endDate));
    }
}
