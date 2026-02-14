package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.SpringAgentMessageLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 短期记忆消息表 Mapper
public interface SpringAgentMessageLogMapper extends BaseMapper<SpringAgentMessageLog> {
}
