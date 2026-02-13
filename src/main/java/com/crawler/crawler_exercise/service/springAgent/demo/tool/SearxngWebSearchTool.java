package com.crawler.crawler_exercise.service.springAgent.demo.tool;

import com.crawler.crawler_exercise.config.SearXNGConfig;
import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class SearxngWebSearchTool {

    private final SearXNGConfig searXNGConfig;
    private final ToolTraceContext toolTraceContext;

    @Value("${spring.ai.demo.top-k:3}")
    private int topK;

    public SearxngWebSearchTool(SearXNGConfig searXNGConfig, ToolTraceContext toolTraceContext) {
        this.searXNGConfig = searXNGConfig;
        this.toolTraceContext = toolTraceContext;
    }

    @Tool(description = "Search latest public information from SearXNG")
    public String webSearch(@ToolParam(description = "Search query") String query) {
        log.info("【Tool触发】开始执行 web_search，query={}", query);
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = searXNGConfig.getUrl() + "/search?q=" + encodedQuery + "&format=json&engines=baidu";
        log.info("【Tool触发】web_search 请求地址={}", url);

        try {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("searkey", searXNGConfig.getKey());
                request.setHeader("User-Agent", "Mozilla/5.0");
                try (CloseableHttpResponse response = client.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    String body = entity == null ? "" : EntityUtils.toString(entity);
                    JsonNode root = new ObjectMapper().readTree(body);
                    JsonNode results = root.path("results");
                    StringBuilder summary = new StringBuilder();
                    for (int i = 0; i < results.size() && i < topK; i++) {
                        JsonNode item = results.get(i);
                        summary.append("标题: ").append(item.path("title").asText()).append("\n")
                                .append("摘要: ").append(item.path("content").asText()).append("\n")
                                .append("链接: ").append(item.path("url").asText()).append("\n\n");
                    }
                    toolTraceContext.addTool("web_search");
                    toolTraceContext.addSource("searxng");
                    String result = summary.toString().trim();
                    log.info("【Tool触发】web_search 执行完成，命中条数={}，结果长度={}", Math.min(results.size(), topK), result.length());
                    log.info("【Tool触发】web_search 输出内容:\n{}", result);
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("【Tool触发】web_search 执行异常: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
