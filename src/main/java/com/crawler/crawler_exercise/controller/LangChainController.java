package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.HuggingFaceConfig;
import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.service.IMilvusEmbeddingService;
import com.crawler.crawler_exercise.service.MyAiAssistant;
import dev.ai4j.openai4j.chat.ResponseFormat;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/LangChain")
@Slf4j
public class LangChainController {

    @Resource
    YunWuConfig yunWuConfig;
    @Resource
    HuggingFaceConfig huggingFaceConfig;

    @Autowired
    IMilvusEmbeddingService milvusEmbeddingService;

    @PostMapping("/langchainInfo")
    public void langchainInfo(@RequestBody String speak) {
        log.info("【用户说】:{}", speak);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        log.info("当前时间:{}", now);

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(yunWuConfig.getUrl())
                .apiKey(yunWuConfig.getKey())
                .modelName("qwen3-1.7b")
                .timeout(Duration.ofSeconds(30))
                .build();
        // 老版基础版本
//        UserMessage userMessage = new UserMessage(speak);
//        ChatRequest build = new ChatRequest.Builder().messages(userMessage).toolSpecifications().build();
//        ChatResponse chat = model.chat(build);
//        String result = chat.aiMessage().toString();
        // 新版本,可以加rag或其他工具
        MyAiAssistant assistant = AiServices.builder(MyAiAssistant.class)
                .systemMessageProvider(obj -> "You are in Beijing. You are a friendly assistant." + "The current time is China Standard Time:" + now)
                .chatLanguageModel(model)
                .tools()
                .build();
        String result = assistant.chat(speak);

        log.info("【AI回复】:{}", result);
    }

    @PostMapping("/insertMilvusInfo")
    public void insertMilvusInfo() {
        milvusEmbeddingService.insertMilvusInfo();
    }

    @PostMapping("/insertMilvusData")
    public String insertMilvusData(@RequestBody String data) {
        try {
            milvusEmbeddingService.insertMilvusData(data);
            return "success";
        } catch (Exception e) {
            return "failed";
        }
    }

    // 从Milvus向量数据库中寻找问题的答案并返回
    @GetMapping("/getMilvusAnswer")
    public String getMilvusAnswer(@RequestParam String problem) {
        String msg = milvusEmbeddingService.getMilvusInfo(problem);

        return "回复内容:" + msg;
    }

    /**
     * Only use RAG information to answer the question.
     *
     * @param speak
     * @return
     */
    @PostMapping("/ragChat")
    public String ragChat(@RequestBody String speak) {
        String ragContext = milvusEmbeddingService.getMilvusInfo(speak);

        String prompt = "Based on this context: " + ragContext +
                "\n\nQuestion: " + speak +
                "\n\nAnswer:";

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(yunWuConfig.getUrl())
                .apiKey(yunWuConfig.getKey())
                .modelName("qwen3-1.7b")
                .timeout(Duration.ofSeconds(30))
                .build();

        // 直接使用模型回答
        UserMessage userMessage = new UserMessage(prompt);
        ChatRequest build = new ChatRequest.Builder().messages(userMessage).build();
        ChatResponse chat = model.chat(build);

        return chat.aiMessage().text();
    }

    /**
     * Use LLM's common sense and RAG content to provide answers.
     *
     * @param speak
     */
    @PostMapping("/langchainRagChat")
    public String langchainRagChat(@RequestBody String speak) {
        log.info("【用户说】:{}", speak);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        log.info("当前时间:{}", now);

        // 1. 先从Milvus检索相关内容
        String ragContext = milvusEmbeddingService.getMilvusInfo(speak);
        log.info("【RAG检索到的内容】:{}", ragContext);

//        OpenAiChatModel model = OpenAiChatModel.builder()
//                .baseUrl(yunWuConfig.getUrl())
//                .apiKey(yunWuConfig.getKey())
//                .modelName("qwen3-1.7b")
//                .timeout(Duration.ofSeconds(30))
//                .build();

        // user localtion LLM to provide answers ~
        // "/v1" is a must for the OpenAI API!
//        OpenAiChatModel model = OpenAiChatModel.builder()
//                .baseUrl("http://localhost:11434/v1")
//                .apiKey("ollama")
//                .modelName("qwen3:1.7b")
//                .timeout(Duration.ofSeconds(30))
//                .build();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://flowercui-eamongptv2.hf.space/v2") // /v2接口只返回assisant后面的内容；/v1会显示think的思考过程
                .apiKey(huggingFaceConfig.getToken())
                .modelName("qwen3-1.7b")
                .temperature(0.7)
                .maxTokens(200)
                .timeout(Duration.ofSeconds(60))
                .build();

        // 2. 构建包含RAG内容的系统提示
        String systemPrompt = "The current time is China Standard Time:" + now +
                "\n\nUse the following retrieved context to help answer questions: " + ragContext +
                "\n\nIf the context is relevant, incorporate it into your response. " +
                "If not relevant, answer based on your general knowledge." +
                "Only the final answer is given, without going back to the thinking or analysis process!";

        MyAiAssistant assistant = AiServices.builder(MyAiAssistant.class)
                .systemMessageProvider(obj -> systemPrompt)
                .chatLanguageModel(model)
                .tools()
                .build();

        String result = assistant.chat(speak);
        return "【AI回复】:" + result;
    }

}
