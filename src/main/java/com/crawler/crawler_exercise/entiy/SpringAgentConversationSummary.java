package com.crawler.crawler_exercise.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("springagent_conversation_summary")
public class SpringAgentConversationSummary {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private String conversationId;

    // 长期记忆摘要文本
    @TableField("summary_text")
    private String summaryText;

    // 摘要更新版本号
    @TableField("summary_version")
    private Integer summaryVersion;

    @TableField("source_turn_from")
    private Integer sourceTurnFrom;

    @TableField("source_turn_to")
    private Integer sourceTurnTo;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
