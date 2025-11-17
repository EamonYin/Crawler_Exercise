package com.crawler.crawler_exercise.utils.tool;

import com.crawler.crawler_exercise.entiy.ChatMessage;
import com.crawler.crawler_exercise.entiy.enums.MessageType;
import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MysqlChatMemory implements ChatMemory {

    private final IChatMessageService chatMessageService;

    @Override
    public void add(String conversationId, Message message) {
        if (message == null) {
            return;
        }
        add(conversationId, Collections.singletonList(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<ChatMessage> toSave = messages.stream().map(m -> {
            ChatMessage cm = new ChatMessage();
            cm.setConversationId(conversationId);
            String text;
            if (m instanceof UserMessage) {
                text = ((UserMessage) m).getText();
                cm.setMessageType(MessageType.USER);
            } else if (m instanceof AssistantMessage) {
                text = ((AssistantMessage) m).getText();
                cm.setMessageType(MessageType.ASSISTANT);
            } else {
                text = String.valueOf(m);
                cm.setMessageType(MessageType.ASSISTANT);
            }
            cm.setContent(text);
            cm.setCreatedAt(LocalDateTime.now());
            // 从消息元数据中提取自定义的 type（如果存在）
            try {
                Object t = m.getMetadata() != null ? m.getMetadata().get("type") : null;
                if (t != null) {
                    cm.setType(Integer.valueOf(String.valueOf(t)));
                }
            } catch (Exception ignore) { }
            return cm;
        }).collect(Collectors.toList());
        chatMessageService.saveAll(conversationId, toSave);
    }

    public void add(String conversationId, List<Message> messages, DashScopeChatInput input) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<ChatMessage> toSave = messages.stream().map(m -> {
            ChatMessage cm = new ChatMessage();
            cm.setConversationId(conversationId);
            String text;
            if (m instanceof UserMessage) {
                text = ((UserMessage) m).getText();
                cm.setMessageType(MessageType.USER);
            } else if (m instanceof AssistantMessage) {
                text = ((AssistantMessage) m).getText();
                cm.setMessageType(MessageType.ASSISTANT);
            } else {
                text = String.valueOf(m);
                cm.setMessageType(MessageType.ASSISTANT);
            }
            cm.setContent(text);
            cm.setCreatedAt(LocalDateTime.now());
            cm.setType(input.getType());
            return cm;
        }).collect(Collectors.toList());
        chatMessageService.saveAll(conversationId, toSave);
    }

    @Override
    public List<Message> get(String conversationId) {
        return chatMessageService.findByConversationId(conversationId).stream()
                .map(cm -> cm.getMessageType() == MessageType.USER
                        ? new UserMessage(cm.getContent())
                        : new AssistantMessage(cm.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        chatMessageService.deleteByConversationId(conversationId);
    }
}
