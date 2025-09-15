package com.crawler.crawler_exercise.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.service.IDashScopeService;
import com.crawler.crawler_exercise.utls.tool.MysqlChatMemory;
import com.crawler.crawler_exercise.utls.tool.TimeTools;
import com.crawler.crawler_exercise.utls.tool.TripPlanTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DashScopeServiceImpl implements IDashScopeService {

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient chatClient;

    public DashScopeServiceImpl(ChatClient.Builder builder, MysqlChatMemory mysqlChatMemory) {
        this.chatClient = builder.defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(mysqlChatMemory).build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }
    
    @Override
    public Flux<String> DashScopeChatByMemory(DashScopeChatInput input) {
        UserMessage user = UserMessage.builder()
                .text(input.getQuestion())
                .metadata(Map.of("type", input.getType()))
                .build();
        return chatClient.prompt(new Prompt(List.of(user)))
                .tools(new TimeTools(),new TripPlanTools())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getConversationId()))
                .stream()
                .content();
    }

}