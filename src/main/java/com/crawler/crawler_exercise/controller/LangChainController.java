package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.config.*;
import com.crawler.crawler_exercise.entiy.LangchainRagChatDTO;
import com.crawler.crawler_exercise.service.IMilvusEmbeddingService;
import com.crawler.crawler_exercise.service.IQwenService;
import com.crawler.crawler_exercise.service.MyAiAssistant;
import com.crawler.crawler_exercise.utls.api.QwenAPi;
import dev.ai4j.openai4j.chat.ResponseFormat;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

// HTTP 客户端相关导入
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;

// JSON 解析相关导入
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/LangChain")
@Slf4j
public class LangChainController {

    @Resource
    YunWuConfig yunWuConfig;
    @Resource
    HuggingFaceConfig huggingFaceConfig;
    @Resource
    SearchApiConfig searchApiConfig;
    @Resource
    SearXNGConfig searXNGConfig;
    @Resource
    EamonGPTConfig eamonGPTConfig;

    @Autowired
    IMilvusEmbeddingService milvusEmbeddingService;
    @Autowired
    IQwenService qwenService;

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

    @PostMapping("/ragChatSearchApi")
    public String ragChatSearchApi(@RequestBody LangchainRagChatDTO langchainRagChatDTO) {
        log.info("【用户说】:{}", langchainRagChatDTO);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        log.info("当前时间:{}", now);

        // 1. 先从Milvus检索相关内容
        String ragContext = milvusEmbeddingService.getMilvusInfo(langchainRagChatDTO.getSpeak());
        log.info("【RAG检索到的内容】:{}", ragContext);

        // 2. 使用SearchAPI进行网络搜索
        String webSearchResults = searchWithSearchApi(langchainRagChatDTO.getSpeak());
        log.info("【SearchAPI搜索结果】:{}", webSearchResults);

        /**
         * /v1 会显示think的思考过程
         * /v2 接口只返回assisant后面的内容
         * /v3 有联网功能（目前无效！）
         */
//        OpenAiChatModel model = OpenAiChatModel.builder()
//                .baseUrl("https://flowercui-eamongptv2.hf.space/v2")
//                .apiKey(huggingFaceConfig.getToken())
//                .modelName("qwen3-1.7b")
//                .temperature(0.7)
//                .maxTokens(200)
//                .timeout(Duration.ofSeconds(60))
//                .build();

                OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:11434/v1")
                .apiKey("ollama")
                .modelName("qwen3:1.7b")
                .timeout(Duration.ofSeconds(30))
                .build();


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
     * 使用SearchAPI进行网络搜索
     * @param query 搜索查询
     * @return 搜索结果摘要
     */
    private String searchWithSearchApi(String query) {
        try {
            // 创建SearchAPI搜索引擎实例
            String apiKey = searchApiConfig.getKey();
            if (apiKey == null || apiKey.equals("your-searchapi-key-here")) {
                return "SearchAPI密钥未配置，请在application.yml中设置searchapi.key";
            }

            WebSearchEngine searchEngine = SearchApiWebSearchEngine.builder()
                    .apiKey(apiKey)
                    .build();

            // 执行搜索
            WebSearchResults searchResults = searchEngine.search(query);

            // 处理搜索结果
            StringBuilder summary = new StringBuilder();
            if (searchResults != null && searchResults.results() != null) {
                searchResults.results().forEach(result -> {
                    summary.append("标题: ").append(result.title()).append("\n");
                    if (result.snippet() != null && !result.snippet().trim().isEmpty()) {
                        summary.append("摘要: ").append(result.snippet()).append("\n");
                    }
                    summary.append("链接: ").append(result.url()).append("\n\n");
                });
            }

            String resultSummary = summary.toString().trim();
            return resultSummary.isEmpty() ? "未找到相关网络信息" : resultSummary;

        } catch (Exception e) {
            log.error("【SearchAPI搜索失败】:{}", e.getMessage());
            return "网络搜索暂时不可用: " + e.getMessage();
        }
    }

    @PostMapping("/ragChatBySearXNG")
    public String ragChatBySearXNG(@RequestBody LangchainRagChatDTO langchainRagChatDTO) {
        log.info("【用户说】:{}", langchainRagChatDTO);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        log.info("当前时间:{}", now);

        // 1. 先从Milvus检索相关内容
        String ragContext = milvusEmbeddingService.getMilvusInfo(langchainRagChatDTO.getSpeak());
        log.info("【RAG检索到的内容】:{}", ragContext);

        // 2. 使用SearXNG进行网络搜索
        String webSearchResults = searchWithSearXNG(langchainRagChatDTO.getSpeak());
        log.info("【SearXNG搜索结果】:{}", webSearchResults);

        // "/v1" is a must for the OpenAI API!
//        OpenAiChatModel model = OpenAiChatModel.builder()
//                .baseUrl("http://localhost:11434/v1")
//                .apiKey("ollama")
//                .modelName("qwen3:1.7b")
//                .timeout(Duration.ofSeconds(30))
//                .build();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(eamonGPTConfig.getUrl())
                .apiKey(eamonGPTConfig.getEamonGPTKey())
                .modelName("qwen3")
                .timeout(Duration.ofSeconds(30))
                .build();

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

        if (result.contains("NOT_LOGIN")) {
            log.error("通义千问token过期!");
//            eamonGPTConfig.upDateEamonGPTKey();
            return "通义千问token过期,请再次请求";
        }

        return "【AI回复】:" + result;
    }

    /**
     * 使用SearXNG进行网络搜索
     * @param query 搜索查询
     * @return 搜索结果摘要
     */
    private String searchWithSearXNG(String query) {
        try {
//            query = URLEncoder.encode(query).replace("+", "%20"); // 将+替换为空格编码
            // 使用 HTTP 请求直接调用 SearXNG API
            String baseUrl = searXNGConfig.getUrl();
            String searchUrl = baseUrl + "/search?q=" + query + "&format=json&engines=baidu";
            log.info("[网络请求]:{}",searchUrl);

            // 创建 HTTP 客户端
            CloseableHttpClient httpClient = HttpClients.createDefault();
            
            // 创建 HTTP GET 请求
            HttpGet httpGet = new HttpGet(searchUrl);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
            httpGet.setHeader("searkey",searXNGConfig.getKey());

            // 执行请求并获取响应
            CloseableHttpResponse response = httpClient.execute(httpGet);
            
            try {
                // 解析响应
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonResponse = EntityUtils.toString(entity);
                    
                    // 解析 JSON 响应
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    
                    // 处理搜索结果
                    StringBuilder summary = new StringBuilder();
                    JsonNode resultsNode = rootNode.get("results");
                    
                    if (resultsNode != null && resultsNode.isArray()) {
                        for (JsonNode resultNode : resultsNode) {
                            String title = resultNode.has("title") ? resultNode.get("title").asText() : "";
                            String snippet = resultNode.has("content") ? resultNode.get("content").asText() : "";
                            String url = resultNode.has("url") ? resultNode.get("url").asText() : "";
                            
                            summary.append("标题: ").append(title).append("\n");
                            if (snippet != null && !snippet.trim().isEmpty()) {
                                summary.append("摘要: ").append(snippet).append("\n");
                            }
                            summary.append("链接: ").append(url).append("\n\n");
                        }
                    }
                    
                    String resultSummary = summary.toString().trim();
                    log.info("[网络请求 结果]:{}",resultSummary);
                    return resultSummary.isEmpty() ? "未找到相关网络信息" : resultSummary;
                }
            } finally {
                response.close();
                httpClient.close();
            }
        } catch (Exception e) {
            log.error("【SearXNG搜索失败】:{}", e.getMessage());
            return "网络搜索暂时不可用: " + e.getMessage();
        }
        
        return "网络搜索暂时不可用";
    }



}
