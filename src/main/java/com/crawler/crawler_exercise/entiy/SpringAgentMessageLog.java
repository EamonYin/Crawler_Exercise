package com.crawler.crawler_exercise.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("springagent_message_log")
public class SpringAgentMessageLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 会话维度主键（业务键）
    @TableField("conversation_id")
    private String conversationId;

    // 用户维度主键（业务键）
    @TableField("user_id")
    private String userId;

    // USER / ASSISTANT / SYSTEM
    @TableField("role")
    private String role;

    // 原始消息内容（短期记忆来源）
    @TableField("content")
    private String content;

    // 会话内轮次（同一轮用户和助手共用turn_no）
    @TableField("turn_no")
    private Integer turnNo;

    @TableField("token_estimate")
    private Integer tokenEstimate;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
