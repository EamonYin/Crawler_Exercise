package com.crawler.crawler_exercise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crawler.crawler_exercise.entiy.ChatMessage;
import com.crawler.crawler_exercise.mapper.ChatMessageMapper;
import com.crawler.crawler_exercise.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Override
    public List<String> findConversationIds() {
        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.select("DISTINCT conversation_id AS conversationId");
        return this.list(qw).stream()
                .map(ChatMessage::getConversationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatMessage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatMessage::getConversationId, conversationId)
           .orderByAsc(ChatMessage::getId);
        return this.list(lqw);
    }

    @Override
    public void saveAll(String conversationId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (ChatMessage m : messages) {
            if (m.getConversationId() == null) {
                m.setConversationId(conversationId);
            }
        }
        this.saveBatch(messages);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatMessage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatMessage::getConversationId, conversationId);
        this.remove(lqw);
    }
}

