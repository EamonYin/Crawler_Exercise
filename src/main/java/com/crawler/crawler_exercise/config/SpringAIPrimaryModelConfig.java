package com.crawler.crawler_exercise.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringAIPrimaryModelConfig {

    // 解决容器内存在多个 ChatModel（OpenAI + DashScope）导致的自动注入歧义。
    // 这里显式把 DashScope 指定为全局默认模型，供 Spring AI 自动配置使用。
    @Bean
    @Primary
    public ChatModel primaryChatModel(@Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel) {
        return dashscopeChatModel;
    }
}
