package com.crawler.crawler_exercise.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring-agent-milvus")
public class SpringAgentMilvusConfig {

    private String url;
    private String collectionName;
    private Integer dimension = 512;

    public EmbeddingStore<TextSegment> getMilvusEmbeddingStore() {
        return MilvusEmbeddingStore.builder()
                .uri(this.url)
                .collectionName(this.collectionName)
                .dimension(this.dimension)
                .build();
    }

    public EmbeddingModel getEmbeddingModel() {
        return new BgeSmallZhV15QuantizedEmbeddingModel();
    }
}
