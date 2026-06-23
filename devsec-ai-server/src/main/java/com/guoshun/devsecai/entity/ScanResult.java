package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scan_result")
public class ScanResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;        // 项目ID
    
    private Long policyId;         // 策略ID
    
    private String branch;         // 扫描分支
    
    private String commitId;       // 提交ID
    
    private Integer status;        // 0-扫描中 1-完成 2-失败
    
    private Integer totalIssues;   // 总问题数
    
    private Integer criticalCount;  // 严重
    
    private Integer highCount;     // 高危
    
    private Integer mediumCount;   // 中危
    
    private Integer lowCount;      // 低危
    
    private Integer infoCount;     // 提示
    
    private String summary;       // 扫描摘要JSON
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
