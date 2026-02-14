package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.SpringAgentConversationSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 长期记忆摘要表 Mapper
public interface SpringAgentConversationSummaryMapper extends BaseMapper<SpringAgentConversationSummary> {
}
