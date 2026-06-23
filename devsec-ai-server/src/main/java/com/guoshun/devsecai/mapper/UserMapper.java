package com.guoshun.devsecai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guoshun.devsecai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(String username);
}
