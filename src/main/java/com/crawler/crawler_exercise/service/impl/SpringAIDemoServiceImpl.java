package com.crawler.crawler_exercise.service.impl;

import com.crawler.crawler_exercise.config.SpringAgentMilvusConfig;
import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.input.SpringAIDemoKbInsertInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoKbInsertOutput;
import com.crawler.crawler_exercise.service.ISpringAIDemoService;
import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.CurrentTimeTool;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.MilvusKnowledgeSearchTool;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.SearxngWebSearchTool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SpringAIDemoServiceImpl implements ISpringAIDemoService {

    private final ChatClient springAIDemoChatClient;
    private final SpringAgentMilvusConfig springAgentMilvusConfig;
    private final CurrentTimeTool currentTimeTool;
    private final MilvusKnowledgeSearchTool milvusKnowledgeSearchTool;
    private final SearxngWebSearchTool searxngWebSearchTool;
    private final ToolTraceContext toolTraceContext;

    // 显式注入 demo 专用 ChatClient，确保该链路固定使用 OpenAI(yunwu) 模型，
    // 不受全局 @Primary ChatModel（DashScope）影响。
    public SpringAIDemoServiceImpl(@Qualifier("springAIDemoChatClient") ChatClient springAIDemoChatClient,
                                   SpringAgentMilvusConfig springAgentMilvusConfig,
                                   CurrentTimeTool currentTimeTool,
                                   MilvusKnowledgeSearchTool milvusKnowledgeSearchTool,
                                   SearxngWebSearchTool searxngWebSearchTool,
                                   ToolTraceContext toolTraceContext) {
        this.springAIDemoChatClient = springAIDemoChatClient;
        this.springAgentMilvusConfig = springAgentMilvusConfig;
        this.currentTimeTool = currentTimeTool;
        this.milvusKnowledgeSearchTool = milvusKnowledgeSearchTool;
        this.searxngWebSearchTool = searxngWebSearchTool;
        this.toolTraceContext = toolTraceContext;
    }

    @Override
    public SpringAIDemoChatOutput demoChat(SpringAIDemoChatInput input) {
        if (input == null || !StringUtils.hasText(input.getQuestion())) {
            throw new IllegalArgumentException("question不能为空");
        }

        log.info("【Demo主流程】收到问题: {}", input.getQuestion());
        // 每次新请求开始前清空 ThreadLocal 里的工具调用痕迹，避免把上一次请求的 usedTools/sources 串到本次响应。
        toolTraceContext.clear();
        String answer = springAIDemoChatClient.prompt()
                .system("""
                        你是一个最小化的Agent Demo。
                        当用户问题涉及今天、现在、当前时间、日期、星期时，必须先调用currentTime工具获取真实当前时间。
                        必须先尝试调用knowledgeSearch检索内部知识库。
                        如果内部知识不足，再调用webSearch进行联网搜索补充。
                        结合工具结果给出最终答案，并在末尾简要标注信息来源。
                        """)
                .user(input.getQuestion())
                .tools(currentTimeTool, milvusKnowledgeSearchTool, searxngWebSearchTool)
                .call()
                .content();

        log.info("【Demo主流程】模型返回完成，usedTools={}, sources={}",
                toolTraceContext.getUsedTools(), toolTraceContext.getSources());

        SpringAIDemoChatOutput output = new SpringAIDemoChatOutput();
        output.setAnswer(answer);
        output.setUsedTools(toolTraceContext.getUsedTools());
        output.setSources(toolTraceContext.getSources());
        log.info("【Demo主流程】响应输出完成，answer长度={}", answer == null ? 0 : answer.length());
        return output;
    }

    @Override
    public SpringAIDemoKbInsertOutput insertKnowledge(SpringAIDemoKbInsertInput input) {
        if (input == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }

        List<String> toInsert = new ArrayList<>();
        if (StringUtils.hasText(input.getContent())) {
            toInsert.add(input.getContent());
        }
        if (input.getContents() != null) {
            for (String item : input.getContents()) {
                if (StringUtils.hasText(item)) {
                    toInsert.add(item);
                }
            }
        }

        if (toInsert.isEmpty()) {
            throw new IllegalArgumentException("content 或 contents 至少传一个");
        }

        EmbeddingStore<TextSegment> embeddingStore = springAgentMilvusConfig.getMilvusEmbeddingStore();
        EmbeddingModel embeddingModel = springAgentMilvusConfig.getEmbeddingModel();
        int insertedCount = 0;
        for (String text : toInsert) {
            TextSegment segment = TextSegment.from(text);
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
            insertedCount++;
        }

        log.info("【知识库入库】完成，collection={}, insertedCount={}", springAgentMilvusConfig.getCollectionName(), insertedCount);
        SpringAIDemoKbInsertOutput output = new SpringAIDemoKbInsertOutput();
        output.setInsertedCount(insertedCount);
        output.setCollectionName(springAgentMilvusConfig.getCollectionName());
        output.setMessage("success");
        return output;
    }
}
