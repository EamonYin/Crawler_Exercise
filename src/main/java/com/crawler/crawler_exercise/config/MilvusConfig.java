package com.crawler.crawler_exercise.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfig {
    private String url;
    private String collectionName;

    public EmbeddingStore<TextSegment> getMilvusEmbeddingStore(){
        // 创建MilvusEmbeddingStore存储对象
        return MilvusEmbeddingStore.builder()
                .uri(this.url) // 连接地址
                .collectionName(this.collectionName) // 集合名称
                .dimension(384) // 向量维度
                .build();
    }

    public EmbeddingModel getEmbeddingModel(){
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    public EmbeddingModel getZhEmbeddingModel(){
        return HuggingFaceEmbeddingModel.builder()
                .modelId("BAAI/bge-large-zh-v1.5") // 中文优化模型
                .accessToken("") // 如果模型是私有的或限速，需要 token
                .build();
    }
}
