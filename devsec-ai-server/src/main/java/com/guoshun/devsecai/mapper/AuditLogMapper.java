package com.guoshun.devsecai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guoshun.devsecai.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
