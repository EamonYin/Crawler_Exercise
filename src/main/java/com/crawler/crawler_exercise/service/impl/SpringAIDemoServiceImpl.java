package com.crawler.crawler_exercise.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crawler.crawler_exercise.config.SpringAgentMilvusConfig;
import com.crawler.crawler_exercise.entiy.SpringAgentConversationSummary;
import com.crawler.crawler_exercise.entiy.SpringAgentMemoryJob;
import com.crawler.crawler_exercise.entiy.SpringAgentMessageLog;
import com.crawler.crawler_exercise.entiy.SpringAgentUserProfile;
import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.input.SpringAIDemoKbInsertInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoKbInsertOutput;
import com.crawler.crawler_exercise.mapper.SpringAgentConversationSummaryMapper;
import com.crawler.crawler_exercise.mapper.SpringAgentMemoryJobMapper;
import com.crawler.crawler_exercise.mapper.SpringAgentMessageLogMapper;
import com.crawler.crawler_exercise.mapper.SpringAgentUserProfileMapper;
import com.crawler.crawler_exercise.service.ISpringAIDemoService;
import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.CurrentTimeTool;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.MilvusKnowledgeSearchTool;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.SearxngWebSearchTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SpringAIDemoServiceImpl implements ISpringAIDemoService {

    // 短期记忆：每次只回放最近N条消息
    private static final int SHORT_TERM_MESSAGE_LIMIT = 12;
    // 每6轮触发一次摘要压缩
    private static final int SUMMARY_TRIGGER_TURNS = 6;
    // 摘要压缩时读取的最近消息窗口
    private static final int SUMMARY_MESSAGE_WINDOW = 24;
    // 每2轮做一次用户画像抽取
    private static final int PROFILE_EXTRACT_TURNS = 2;

    //配置
    private final ChatClient springAIDemoChatClient;
    private final SpringAgentMilvusConfig springAgentMilvusConfig;
    private final ToolTraceContext toolTraceContext;

    // tool calling
    private final CurrentTimeTool currentTimeTool;
    private final MilvusKnowledgeSearchTool milvusKnowledgeSearchTool;
    private final SearxngWebSearchTool searxngWebSearchTool;

    // 记忆入库的增删改查
    private final SpringAgentMessageLogMapper springAgentMessageLogMapper;
    private final SpringAgentConversationSummaryMapper springAgentConversationSummaryMapper;
    private final SpringAgentUserProfileMapper springAgentUserProfileMapper;
    private final SpringAgentMemoryJobMapper springAgentMemoryJobMapper;
    private final ObjectMapper objectMapper;

    // 显式注入 demo 专用 ChatClient，确保该链路固定使用 OpenAI(yunwu) 模型，
    // 不受全局 @Primary ChatModel（DashScope）影响。
    public SpringAIDemoServiceImpl(@Qualifier("springAIDemoChatClient") ChatClient springAIDemoChatClient,
                                   SpringAgentMilvusConfig springAgentMilvusConfig,
                                   CurrentTimeTool currentTimeTool,
                                   MilvusKnowledgeSearchTool milvusKnowledgeSearchTool,
                                   SearxngWebSearchTool searxngWebSearchTool,
                                   ToolTraceContext toolTraceContext,
                                   SpringAgentMessageLogMapper springAgentMessageLogMapper,
                                   SpringAgentConversationSummaryMapper springAgentConversationSummaryMapper,
                                   SpringAgentUserProfileMapper springAgentUserProfileMapper,
                                   SpringAgentMemoryJobMapper springAgentMemoryJobMapper,
                                   ObjectMapper objectMapper) {
        this.springAIDemoChatClient = springAIDemoChatClient;
        this.springAgentMilvusConfig = springAgentMilvusConfig;
        this.currentTimeTool = currentTimeTool;
        this.milvusKnowledgeSearchTool = milvusKnowledgeSearchTool;
        this.searxngWebSearchTool = searxngWebSearchTool;
        this.toolTraceContext = toolTraceContext;
        this.springAgentMessageLogMapper = springAgentMessageLogMapper;
        this.springAgentConversationSummaryMapper = springAgentConversationSummaryMapper;
        this.springAgentUserProfileMapper = springAgentUserProfileMapper;
        this.springAgentMemoryJobMapper = springAgentMemoryJobMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public SpringAIDemoChatOutput demoChat(SpringAIDemoChatInput input) {
        if (input == null || !StringUtils.hasText(input.getQuestion()) || !StringUtils.hasText(input.getUserId())) {
            throw new IllegalArgumentException("userId 和 question 不能为空");
        }

        String conversationId = resolveConversationId(input.getConversationId());
        String userId = input.getUserId().trim();
        String question = input.getQuestion().trim();
        int turnNo = nextTurnNo(conversationId);

        //【记忆相关】
        // 三层记忆读取：短期消息 + 长期摘要 + 用户画像
        // 短期保证“当前对话连贯”，摘要保证“历史不丢”，画像保证“用户特征不丢”。
        List<SpringAgentMessageLog> shortTermMessages = listRecentMessages(conversationId, SHORT_TERM_MESSAGE_LIMIT);
        SpringAgentConversationSummary summary = getConversationSummary(conversationId);
        List<SpringAgentUserProfile> profileFacts = listUserProfileFacts(userId);
        String memorySystemPrompt = buildMemoryPrompt(profileFacts, summary, shortTermMessages);

        //【记忆相关】
        // 先落库用户消息，再调用模型
        log.info("【Demo主流程】收到问题, conversationId={}, userId={}, turnNo={}", conversationId, userId, turnNo);
        persistMessage(conversationId, userId, "USER", question, turnNo);

        toolTraceContext.clear();
        String answer = springAIDemoChatClient.prompt()
                .system("""
                        你是一个最小化的Agent Demo。
                        当用户问题涉及今天、现在、当前时间、日期、星期时，必须先调用currentTime工具获取真实当前时间。
                        必须先尝试调用knowledgeSearch检索内部知识库。
                        如果内部知识不足，再调用webSearch进行联网搜索补充。
                        结合工具结果给出最终答案，并在末尾简要标注信息来源。
                        """)
                .system(memorySystemPrompt)
                .user(question)
                .tools(currentTimeTool, milvusKnowledgeSearchTool, searxngWebSearchTool)
                .call()
                .content();

        //【记忆相关】
        String finalAnswer = answer == null ? "" : answer;
        // 模型回复也落库，作为后续短期记忆来源
        persistMessage(conversationId, userId, "ASSISTANT", finalAnswer, turnNo);

        //【记忆相关】
        // 到达阈值后增量维护长期摘要和用户画像
        if (turnNo % SUMMARY_TRIGGER_TURNS == 0) {
            refreshConversationSummary(conversationId, turnNo);
        }
        if (turnNo % PROFILE_EXTRACT_TURNS == 0) {
            extractAndUpsertUserProfile(userId, conversationId, turnNo);
        }

        log.info("【Demo主流程】模型返回完成，conversationId={}, usedTools={}, sources={}",
                conversationId, toolTraceContext.getUsedTools(), toolTraceContext.getSources());

        SpringAIDemoChatOutput output = new SpringAIDemoChatOutput();
        output.setConversationId(conversationId);
        output.setAnswer(finalAnswer);
        output.setUsedTools(toolTraceContext.getUsedTools());
        output.setSources(toolTraceContext.getSources());
        log.info("【Demo主流程】响应输出完成，answer长度={}", finalAnswer.length());
        return output;
    }

    //【记忆相关】
    private String resolveConversationId(String inputConversationId) {
        if (StringUtils.hasText(inputConversationId)) {
            return inputConversationId.trim();
        }
        // 首次会话由后端生成新的会话ID
        return UUID.randomUUID().toString().replace("-", "");
    }

    //【记忆相关】
    private int nextTurnNo(String conversationId) {
        LambdaQueryWrapper<SpringAgentMessageLog> qw = new LambdaQueryWrapper<>();
        qw.eq(SpringAgentMessageLog::getConversationId, conversationId)
                .orderByDesc(SpringAgentMessageLog::getTurnNo)
                .orderByDesc(SpringAgentMessageLog::getId)
                .last("limit 1");
        SpringAgentMessageLog latest = springAgentMessageLogMapper.selectOne(qw);
        if (latest == null || latest.getTurnNo() == null) {
            return 1;
        }
        return latest.getTurnNo() + 1;
    }

    //【记忆相关】
    // 查询会话最近N条消息，作为短期记忆回放
    private List<SpringAgentMessageLog> listRecentMessages(String conversationId, int limit) {
        LambdaQueryWrapper<SpringAgentMessageLog> qw = new LambdaQueryWrapper<>();
        qw.eq(SpringAgentMessageLog::getConversationId, conversationId)
                .orderByDesc(SpringAgentMessageLog::getTurnNo)
                .orderByDesc(SpringAgentMessageLog::getId)
                .last("limit " + limit);
        List<SpringAgentMessageLog> rows = springAgentMessageLogMapper.selectList(qw);
        Collections.reverse(rows);
        return rows;
    }

    //【记忆相关】
    // 查询会话最新摘要，作为长期记忆
    private SpringAgentConversationSummary getConversationSummary(String conversationId) {
        LambdaQueryWrapper<SpringAgentConversationSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(SpringAgentConversationSummary::getConversationId, conversationId).last("limit 1");
        return springAgentConversationSummaryMapper.selectOne(qw);
    }

    //【记忆相关】
    // 查询用户画像事实，作为个性化记忆
    private List<SpringAgentUserProfile> listUserProfileFacts(String userId) {
        LambdaQueryWrapper<SpringAgentUserProfile> qw = new LambdaQueryWrapper<>();
        qw.eq(SpringAgentUserProfile::getUserId, userId)
                .orderByDesc(SpringAgentUserProfile::getUpdatedAt)
                .last("limit 20");
        return springAgentUserProfileMapper.selectList(qw);
    }

    //【记忆相关】
    // 将三层记忆拼接为系统提示词文本
    private String buildMemoryPrompt(List<SpringAgentUserProfile> profileFacts,
                                     SpringAgentConversationSummary summary,
                                     List<SpringAgentMessageLog> shortTermMessages) {
        // 把三层记忆拼成系统提示词，让模型在本轮回答时参考
        StringBuilder sb = new StringBuilder();
        sb.append("以下是对话记忆，请作为参考；若与用户当前明确陈述冲突，以当前陈述为准。\n");

        sb.append("【用户画像】\n");
        if (profileFacts.isEmpty()) {
            sb.append("无\n");
        } else {
            for (SpringAgentUserProfile fact : profileFacts) {
                sb.append(fact.getFactKey()).append(": ").append(fact.getFactValue()).append("\n");
            }
        }

        sb.append("【会话摘要】\n");
        if (summary == null || !StringUtils.hasText(summary.getSummaryText())) {
            sb.append("无\n");
        } else {
            sb.append(summary.getSummaryText()).append("\n");
        }

        sb.append("【最近对话】\n");
        if (shortTermMessages.isEmpty()) {
            sb.append("无\n");
        } else {
            for (SpringAgentMessageLog message : shortTermMessages) {
                sb.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
            }
        }
        return sb.toString();
    }

    //【记忆相关】
    // 记忆落库
    private void persistMessage(String conversationId, String userId, String role, String content, int turnNo) {
        SpringAgentMessageLog row = new SpringAgentMessageLog();
        row.setConversationId(conversationId);
        row.setUserId(userId);
        row.setRole(role);
        row.setContent(content);
        row.setTurnNo(turnNo);
        row.setTokenEstimate(content == null ? 0 : Math.max(1, content.length() / 4));
        row.setCreatedAt(LocalDateTime.now());
        springAgentMessageLogMapper.insert(row);
    }

    //【记忆相关】
    // 记忆摘要落库
    private void refreshConversationSummary(String conversationId, int turnNo) {
        // 会话级摘要压缩任务
        SpringAgentMemoryJob job = createMemoryJob(conversationId, "SUMMARY", turnNo);
        try {
            List<SpringAgentMessageLog> recentMessages = listRecentMessages(conversationId, SUMMARY_MESSAGE_WINDOW);
            if (recentMessages.isEmpty()) {
                markJobSuccess(job);
                return;
            }

            SpringAgentConversationSummary existing = getConversationSummary(conversationId);
            String oldSummary = existing == null ? "" : existing.getSummaryText();
            String transcript = buildTranscript(recentMessages);
            String newSummary = springAIDemoChatClient.prompt()
                    .system("你是对话摘要助手。输出简洁中文摘要，保留目标、约束、已完成事项和待办事项。只输出摘要正文。")
                    .user("已有摘要：\n" + oldSummary + "\n\n新增对话：\n" + transcript + "\n\n请生成新的合并摘要。")
                    .call()
                    .content();

            if (!StringUtils.hasText(newSummary)) {
                markJobSuccess(job);
                return;
            }

            int fromTurn = recentMessages.get(0).getTurnNo() == null ? 1 : recentMessages.get(0).getTurnNo();
            if (existing == null) {
                SpringAgentConversationSummary row = new SpringAgentConversationSummary();
                row.setConversationId(conversationId);
                row.setSummaryText(newSummary.trim());
                row.setSummaryVersion(1);
                row.setSourceTurnFrom(fromTurn);
                row.setSourceTurnTo(turnNo);
                springAgentConversationSummaryMapper.insert(row);
            } else {
                existing.setSummaryText(newSummary.trim());
                existing.setSummaryVersion((existing.getSummaryVersion() == null ? 0 : existing.getSummaryVersion()) + 1);
                existing.setSourceTurnFrom(fromTurn);
                existing.setSourceTurnTo(turnNo);
                springAgentConversationSummaryMapper.updateById(existing);
            }
            markJobSuccess(job);
        } catch (Exception ex) {
            log.error("【记忆摘要】更新失败, conversationId={}, turnNo={}", conversationId, turnNo, ex);
            markJobFailed(job, ex.getMessage());
        }
    }

    //【记忆相关】
    private void extractAndUpsertUserProfile(String userId, String conversationId, int turnNo) {
        // 用户画像抽取任务（最小版仅抽3个事实键）
        SpringAgentMemoryJob job = createMemoryJob(conversationId, "PROFILE_EXTRACT", turnNo);
        try {
            List<SpringAgentMessageLog> recentMessages = listRecentMessages(conversationId, SHORT_TERM_MESSAGE_LIMIT);
            if (recentMessages.isEmpty()) {
                markJobSuccess(job);
                return;
            }
            String transcript = buildTranscript(recentMessages);
            String rawJson = springAIDemoChatClient.prompt()
                    .system("""
                            你是信息抽取助手。
                            从对话中提取用户稳定事实，仅提取以下键：nickname、location、preference_topic。
                            必须输出JSON对象，格式如下：
                            {"nickname":{"value":"","confidence":0},"location":{"value":"","confidence":0},"preference_topic":{"value":"","confidence":0}}
                            没有就填空字符串和0，不要输出额外文本。
                            """)
                    .user("对话内容：\n" + transcript)
                    .call()
                    .content();
            if (!StringUtils.hasText(rawJson)) {
                markJobSuccess(job);
                return;
            }

            JsonNode root = objectMapper.readTree(extractJson(rawJson));
            log.info("【用户画像】抽取{}", JSON.toJSONString(root));
            upsertProfileFact(userId, conversationId, "nickname", root.path("nickname"));
            upsertProfileFact(userId, conversationId, "location", root.path("location"));
            upsertProfileFact(userId, conversationId, "preference_topic", root.path("preference_topic"));
            markJobSuccess(job);
        } catch (Exception ex) {
            log.error("【用户画像】抽取失败, conversationId={}, turnNo={}", conversationId, turnNo, ex);
            markJobFailed(job, ex.getMessage());
        }
    }

    //【记忆相关】
    private String buildTranscript(List<SpringAgentMessageLog> messages) {
        StringBuilder sb = new StringBuilder();
        for (SpringAgentMessageLog message : messages) {
            sb.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
        }
        return sb.toString();
    }

    //【记忆相关】
    private String extractJson(String rawText) {
        // 容错处理：从模型文本中截取首尾JSON
        int start = rawText.indexOf("{");
        int end = rawText.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return rawText.substring(start, end + 1);
        }
        return "{}";
    }

    //【记忆相关】
    private void upsertProfileFact(String userId, String conversationId, String factKey, JsonNode node) {
        String factValue = node.path("value").asText("");
        if (!StringUtils.hasText(factValue)) {
            return;
        }
        BigDecimal confidence = BigDecimal.valueOf(node.path("confidence").asDouble(0.8d));
        if (confidence.compareTo(BigDecimal.ZERO) < 0) {
            confidence = BigDecimal.ZERO;
        }
        if (confidence.compareTo(BigDecimal.ONE) > 0) {
            confidence = BigDecimal.ONE;
        }

        // 按(user_id, fact_key)更新或插入
        LambdaQueryWrapper<SpringAgentUserProfile> qw = new LambdaQueryWrapper<>();
        qw.eq(SpringAgentUserProfile::getUserId, userId).eq(SpringAgentUserProfile::getFactKey, factKey).last("limit 1");
        SpringAgentUserProfile existing = springAgentUserProfileMapper.selectOne(qw);
        if (existing == null) {
            SpringAgentUserProfile row = new SpringAgentUserProfile();
            row.setUserId(userId);
            row.setFactKey(factKey);
            row.setFactValue(factValue.trim());
            row.setConfidence(confidence);
            row.setSourceConversationId(conversationId);
            row.setLastSeenAt(LocalDateTime.now());
            springAgentUserProfileMapper.insert(row);
        } else {
            existing.setFactValue(factValue.trim());
            existing.setConfidence(confidence);
            existing.setSourceConversationId(conversationId);
            existing.setLastSeenAt(LocalDateTime.now());
            springAgentUserProfileMapper.updateById(existing);
        }
    }

    //【记忆相关】
    private SpringAgentMemoryJob createMemoryJob(String conversationId, String jobType, int turnNo) {
        SpringAgentMemoryJob job = new SpringAgentMemoryJob();
        job.setConversationId(conversationId);
        job.setJobType(jobType);
        job.setTriggerTurnNo(turnNo);
        job.setStatus("RUNNING");
        springAgentMemoryJobMapper.insert(job);
        return job;
    }

    //【记忆相关】
    private void markJobSuccess(SpringAgentMemoryJob job) {
        if (job == null || job.getId() == null) {
            return;
        }
        job.setStatus("SUCCESS");
        job.setErrorMessage(null);
        springAgentMemoryJobMapper.updateById(job);
    }

    //【记忆相关】
    private void markJobFailed(SpringAgentMemoryJob job, String errorMessage) {
        if (job == null || job.getId() == null) {
            return;
        }
        job.setStatus("FAILED");
        if (errorMessage != null && errorMessage.length() > 500) {
            job.setErrorMessage(errorMessage.substring(0, 500));
        } else {
            job.setErrorMessage(errorMessage);
        }
        springAgentMemoryJobMapper.updateById(job);
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
