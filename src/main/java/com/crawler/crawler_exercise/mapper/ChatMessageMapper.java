package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}

