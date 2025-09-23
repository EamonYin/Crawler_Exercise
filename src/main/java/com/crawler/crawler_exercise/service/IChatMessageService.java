package com.crawler.crawler_exercise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crawler.crawler_exercise.entiy.ChatMessage;

import java.util.List;

public interface IChatMessageService extends IService<ChatMessage> {

    List<String> findConversationIds();

    List<ChatMessage> findByConversationId(String conversationId);

    void saveAll(String conversationId, List<ChatMessage> messages);

    void deleteByConversationId(String conversationId);
}

