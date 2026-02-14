package com.crawler.crawler_exercise.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("springagent_user_profile")
public class SpringAgentUserProfile {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("fact_key")
    private String factKey;

    // 用户稳定事实值（如昵称、城市、偏好）
    @TableField("fact_value")
    private String factValue;

    // 事实置信度，范围0~1
    @TableField("confidence")
    private BigDecimal confidence;

    @TableField("source_conversation_id")
    private String sourceConversationId;

    @TableField("last_seen_at")
    private LocalDateTime lastSeenAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
