package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.YunWuConfig;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
        OpenAiChatModel demo = OpenAiChatModel.builder()
                .baseUrl("https://yunwu.ai/v1")
                .apiKey(yunWuConfig.getKey())
                .modelName("qwen3-1.7b")
                .timeout(Duration.ofSeconds(30))
                .build();
        UserMessage userMessage = new UserMessage(speak);
        String result = demo.generate(userMessage).content().toString();
        log.info("【AI回复】:{}", result);
    }

}
