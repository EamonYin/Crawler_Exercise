package com.crawler.crawler_exercise.service.impl;

import com.crawler.crawler_exercise.config.MilvusConfig;
import com.crawler.crawler_exercise.service.IMilvusEmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;

import java.util.List;

@Service
@Slf4j
public class MilvusEmbeddingServiceImpl implements IMilvusEmbeddingService {

    @Autowired
    private MilvusConfig milvusConfig;

    @Override
    public void insertMilvusInfo() {
        // 创建MilvusEmbeddingStore存储对象
        EmbeddingStore<TextSegment> embeddingStore = milvusConfig.getMilvusEmbeddingStore();
        // 嵌入模型
        EmbeddingModel embeddingModel = milvusConfig.getEmbeddingModel();
        // 将数据向量化并存入milvus
        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);
    }

    @Override
    public String getMilvusInfo(String problem) {
        // 创建MilvusEmbeddingStore存储对象
        EmbeddingStore<TextSegment> embeddingStore = milvusConfig.getMilvusEmbeddingStore();
        // 嵌入模型
        EmbeddingModel embeddingModel = milvusConfig.getEmbeddingModel();
        // 搜索
        Embedding queryEmbedding = embeddingModel.embed(problem).content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(embeddingSearchRequest).matches();
        EmbeddingMatch<TextSegment> embeddingMatch = matches.get(0);

        log.info("/getMilvusInfo score:{},text:{}",embeddingMatch.score(),embeddingMatch.embedded().text());

        return embeddingMatch.embedded().text();
    }
}
