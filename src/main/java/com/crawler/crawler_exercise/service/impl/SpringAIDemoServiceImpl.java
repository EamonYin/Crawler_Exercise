package com.crawler.crawler_exercise.service.impl;

import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;
import com.crawler.crawler_exercise.service.ISpringAIDemoService;
import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.MilvusKnowledgeSearchTool;
import com.crawler.crawler_exercise.service.springAgent.demo.tool.SearxngWebSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class SpringAIDemoServiceImpl implements ISpringAIDemoService {

    private final ChatClient springAIDemoChatClient;
    private final MilvusKnowledgeSearchTool milvusKnowledgeSearchTool;
    private final SearxngWebSearchTool searxngWebSearchTool;
    private final ToolTraceContext toolTraceContext;

    // 显式注入 demo 专用 ChatClient，确保该链路固定使用 OpenAI(yunwu) 模型，
    // 不受全局 @Primary ChatModel（DashScope）影响。
    public SpringAIDemoServiceImpl(@Qualifier("springAIDemoChatClient") ChatClient springAIDemoChatClient,
                                   MilvusKnowledgeSearchTool milvusKnowledgeSearchTool,
                                   SearxngWebSearchTool searxngWebSearchTool,
                                   ToolTraceContext toolTraceContext) {
        this.springAIDemoChatClient = springAIDemoChatClient;
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
        toolTraceContext.clear();
        String answer = springAIDemoChatClient.prompt()
                .system("""
                        你是一个最小化的Agent Demo。
                        必须先尝试调用knowledgeSearch检索内部知识库。
                        如果内部知识不足，再调用webSearch进行联网搜索补充。
                        结合工具结果给出最终答案，并在末尾简要标注信息来源。
                        """)
                .user(input.getQuestion())
                .tools(milvusKnowledgeSearchTool, searxngWebSearchTool)
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
}
