package com.crawler.crawler_exercise.service.springAgent.demo.tool;

import com.crawler.crawler_exercise.config.SpringAgentMilvusConfig;
import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MilvusKnowledgeSearchTool {

    private final SpringAgentMilvusConfig springAgentMilvusConfig;
    private final ToolTraceContext toolTraceContext;

    @Value("${spring.ai.demo.top-k:3}")
    private int topK;

    public MilvusKnowledgeSearchTool(SpringAgentMilvusConfig springAgentMilvusConfig, ToolTraceContext toolTraceContext) {
        this.springAgentMilvusConfig = springAgentMilvusConfig;
        this.toolTraceContext = toolTraceContext;
    }

    @Tool(description = "Search internal knowledge base from Milvus with user question")
    public String knowledgeSearch(@ToolParam(description = "User question for retrieval") String query) {
        log.info("【Tool触发】开始执行 knowledge_search，query={}", query);
        EmbeddingStore<TextSegment> embeddingStore = springAgentMilvusConfig.getMilvusEmbeddingStore();
        EmbeddingModel embeddingModel = springAgentMilvusConfig.getEmbeddingModel();
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
        StringBuilder builder = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            builder.append(match.embedded().text()).append("\n");
        }

        toolTraceContext.addTool("knowledge_search");
        toolTraceContext.addSource("milvus:" + springAgentMilvusConfig.getCollectionName());
        String result = builder.toString().trim();
        log.info("【Tool触发】knowledge_search 执行完成，命中条数={}，结果长度={}", matches.size(), result.length());
        return result;
    }
}
