package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.SpringAgentMemoryJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 记忆任务状态表 Mapper
public interface SpringAgentMemoryJobMapper extends BaseMapper<SpringAgentMemoryJob> {
}
