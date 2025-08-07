package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.HuggingFaceConfig;
import com.crawler.crawler_exercise.config.YunWuConfig;
import com.crawler.crawler_exercise.entiy.LangchainRagChatDTO;
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

        /**
         * /v1 会显示think的思考过程
         * /v2 接口只返回assisant后面的内容
         * /v3 有联网功能（目前无效！）
         */
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://flowercui-eamongptv2.hf.space/v2")
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

    @PostMapping("/ragChatByWeb")
    public String ragChatByWeb(@RequestBody LangchainRagChatDTO langchainRagChatDTO) {
        log.info("【用户说】:{}", langchainRagChatDTO);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        log.info("当前时间:{}", now);

        // 1. 先从Milvus检索相关内容
        String ragContext = milvusEmbeddingService.getMilvusInfo(langchainRagChatDTO.getSpeak());
        log.info("【RAG检索到的内容】:{}", ragContext);

        // 2. 使用DuckDuckGo搜索获取网络信息
        /**
         * 信息提取
         * 提取搜索结果的 Abstract 摘要信息
         * 提取 RelatedTopics 中的相关主题（最多3个）
         * 智能过滤空白和无效内容
         */
        String webSearchResults = searchDuckDuckGo(langchainRagChatDTO.getSpeak());
        log.info("【网络搜索结果】:{}", webSearchResults);

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://flowercui-eamongptv2.hf.space/v2")
                .apiKey(huggingFaceConfig.getToken())
                .modelName("qwen3-1.7b")
                .temperature(0.7)
                .maxTokens(300)
                .timeout(Duration.ofSeconds(60))
                .build();

//                OpenAiChatModel model = OpenAiChatModel.builder()
//                .baseUrl("http://localhost:11434/v1")
//                .apiKey("ollama")
//                .modelName("qwen3:1.7b")
//                .timeout(Duration.ofSeconds(30))
//                .build();

        // 3. 构建包含RAG内容和网络搜索结果的系统提示
        String systemPrompt = "The current time is China Standard Time:" + now +
                "\n\nUser Question is: " + langchainRagChatDTO.getSpeak() +
                "\n\nRAG Context: " + ragContext +
                "\n\nWeb Search Results: " + webSearchResults +
                "\n\nUse both the RAG context and web search results to provide a comprehensive answer. " +
                "Prioritize recent information from web search when relevant. " +
                "Only provide the final answer without showing the thinking process!";

        MyAiAssistant assistant = AiServices.builder(MyAiAssistant.class)
                .systemMessageProvider(obj -> systemPrompt)
                .chatLanguageModel(model)
                .tools()
                .build();

        String result = assistant.chat(langchainRagChatDTO.getSpeak());
        return "【AI回复】:" + result;
    }

    /**
     * 使用DuckDuckGo搜索引擎搜索信息
     * @param query 搜索查询
     * @return 搜索结果摘要
     */
    private String searchDuckDuckGo(String query) {
        try {
            // 构建DuckDuckGo搜索URL
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String searchUrl = "https://api.duckduckgo.com/?q=" + encodedQuery + "&format=json&no_html=1&skip_disambig=1";

            log.info("【搜索URL】:{}", searchUrl);

            // 发送HTTP请求
            java.net.URL url = new java.net.URL(searchUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 读取响应
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // 解析JSON响应并提取有用信息
            String jsonResponse = response.toString();
            log.info("【DuckDuckGo原始响应】:{}", jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse);

            // 简单的JSON解析，提取Abstract和RelatedTopics
            String searchSummary = extractSearchSummary(jsonResponse);

            return searchSummary.isEmpty() ? "未找到相关网络信息" : searchSummary;

        } catch (Exception e) {
            log.error("【DuckDuckGo搜索失败】:{}", e.getMessage());
            return "网络搜索暂时不可用";
        }
    }

    /**
     * 从DuckDuckGo JSON响应中提取搜索摘要
     * @param jsonResponse JSON响应字符串
     * @return 搜索摘要
     */
    private String extractSearchSummary(String jsonResponse) {
        StringBuilder summary = new StringBuilder();

        try {
            // 提取Abstract字段
            if (jsonResponse.contains("\"Abstract\":\"") && !jsonResponse.contains("\"Abstract\":\"\"")) {
                int abstractStart = jsonResponse.indexOf("\"Abstract\":\"") + 12;
                int abstractEnd = jsonResponse.indexOf("\"", abstractStart);
                if (abstractEnd > abstractStart) {
                    String abstractText = jsonResponse.substring(abstractStart, abstractEnd);
                    if (!abstractText.trim().isEmpty()) {
                        summary.append("摘要: ").append(abstractText).append("\n");
                    }
                }
            }

            // 提取RelatedTopics中的前几个相关主题
            if (jsonResponse.contains("\"RelatedTopics\":[")) {
                int topicsStart = jsonResponse.indexOf("\"RelatedTopics\":[");
                int topicsEnd = jsonResponse.indexOf("]", topicsStart);
                if (topicsEnd > topicsStart) {
                    String topicsSection = jsonResponse.substring(topicsStart, topicsEnd);
                    // 简单提取Text字段
                    int count = 0;
                    int searchFrom = 0;
                    while (count < 3 && searchFrom < topicsSection.length()) {
                        int textStart = topicsSection.indexOf("\"Text\":\"", searchFrom);
                        if (textStart == -1) break;
                        textStart += 8;
                        int textEnd = topicsSection.indexOf("\"", textStart);
                        if (textEnd > textStart) {
                            String text = topicsSection.substring(textStart, textEnd);
                            if (!text.trim().isEmpty() && text.length() > 10) {
                                summary.append("相关信息").append(count + 1).append(": ").append(text).append("\n");
                                count++;
                            }
                        }
                        searchFrom = textEnd;
                    }
                }
            }

        } catch (Exception e) {
            log.error("【解析搜索结果失败】:{}", e.getMessage());
        }

        return summary.toString().trim();
    }

}
