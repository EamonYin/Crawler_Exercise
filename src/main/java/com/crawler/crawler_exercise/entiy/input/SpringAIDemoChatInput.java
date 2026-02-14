package com.crawler.crawler_exercise.entiy.input;

import lombok.Data;

@Data
public class SpringAIDemoChatInput {
    // 用户ID，用于关联用户画像
    private String userId;
    // 会话ID；首次可不传，后端生成后回传
    private String conversationId;
    // 本轮用户问题
    private String question;
}
