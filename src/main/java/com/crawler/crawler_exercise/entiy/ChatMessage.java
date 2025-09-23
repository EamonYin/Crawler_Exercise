package com.crawler.crawler_exercise.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crawler.crawler_exercise.entiy.enums.MessageType;
import lombok.Data;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

@Data
@TableName("chat_messages")
public class ChatMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField(value = "message_type", typeHandler = EnumTypeHandler.class)
    private MessageType messageType;

    @TableField("content")
    private String content;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("type")
    private Integer type;
}

