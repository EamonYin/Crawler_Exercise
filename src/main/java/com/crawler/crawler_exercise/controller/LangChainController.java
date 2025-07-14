package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.service.MyAiAssistant;
import dev.ai4j.openai4j.chat.ResponseFormat;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;

@RestController
@RequestMapping("/LangChain")
@Slf4j
public class LangChainController {

    @Resource
    YunWuConfig yunWuConfig;

    @PostMapping("/langchainInfo")
    public void langchainInfo(@RequestBody String speak) {
        log.info("【用户说】:{}", speak);
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://yunwu.ai/v1")
                .apiKey(yunWuConfig.getKey())
                .modelName("qwen3-1.7b")
                .timeout(Duration.ofSeconds(30))
                .build();

//        UserMessage userMessage = new UserMessage(speak);
//        ChatRequest build = new ChatRequest.Builder().messages(userMessage).toolSpecifications().build();
//        ChatResponse chat = model.chat(build);
//        String result = chat.aiMessage().toString();

        MyAiAssistant assistant = AiServices.builder(MyAiAssistant.class)
                .systemMessageProvider(obj -> "You are in Beijing. You are a friendly assistant.")
                .chatLanguageModel(model)
                .tools()
                .build();
        String result = assistant.chat(speak);

        log.info("【AI回复】:{}", result);
    }

}
