package com.crawler.crawler_exercise.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SpringAIDemoConfig {

    // Demo 专用 OpenAI 模型（yunwu OpenAI 兼容接口），不作为全局默认模型。
    @Bean
    public OpenAiChatModel springAIDemoOpenAiChatModel(YunWuConfig yunWuConfig,
                                                        @Value("${spring.ai.demo.model:qwen3}") String model) {
        if (!StringUtils.hasText(yunWuConfig.getKey())) {
            throw new IllegalArgumentException("yunwu.key 未配置");
        }

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://yunwu.ai")
                .apiKey(yunWuConfig.getKey())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.2)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    // 给 Demo ChatClient 一个明确限定名，避免与自动配置产生的默认 ChatClient 混用。
    @Bean
    @Qualifier("springAIDemoChatClient")
    public ChatClient springAIDemoChatClient(OpenAiChatModel springAIDemoOpenAiChatModel) {
        return ChatClient.builder(springAIDemoOpenAiChatModel).build();
    }
}
