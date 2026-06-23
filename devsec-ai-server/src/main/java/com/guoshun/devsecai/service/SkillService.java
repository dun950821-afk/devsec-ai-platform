package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.Skill;
import com.guoshun.devsecai.mapper.SkillMapper;
import org.springframework.stereotype.Service;

@Service
public class SkillService {
    
    private final SkillMapper skillMapper;
    
    public SkillService(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }
    
    public Page<Skill> getPage(Integer current, Integer size, String keyword, String type) {
        Page<Skill> page = new Page<>(current, size);
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Skill::getName, keyword).or().like(Skill::getSkillKey, keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Skill::getType, type);
        }
        wrapper.orderByDesc(Skill::getCreateTime);
        return skillMapper.selectPage(page, wrapper);
    }
    
    public Skill getById(Long id) {
        return skillMapper.selectById(id);
    }
    
    public void create(Skill skill) {
        skillMapper.insert(skill);
    }
    
    public void update(Skill skill) {
        skillMapper.updateById(skill);
    }
    
    public void delete(Long id) {
        skillMapper.deleteById(id);
    }
    
    public void updateStatus(Long id, Integer status) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setStatus(status);
        skillMapper.updateById(skill);
    }
}
