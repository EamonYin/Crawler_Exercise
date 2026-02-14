-- 表1：springagent_message_log
-- 作用：存储每一轮原始对话消息（短期记忆来源），用于拼接最近N轮上下文。
CREATE TABLE `springagent_message_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID，同一轮连续对话保持一致',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID，用于区分不同用户',
  `role` VARCHAR(16) NOT NULL COMMENT '消息角色：USER/ASSISTANT/SYSTEM',
  `content` TEXT NOT NULL COMMENT '消息正文内容',
  `turn_no` INT NOT NULL COMMENT '会话内轮次，从1开始递增',
  `token_estimate` INT DEFAULT NULL COMMENT '该消息预估Token数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_turn` (`conversation_id`, `turn_no`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SpringAgent三层记忆-原始消息表（短期记忆）';


-- 表2：springagent_conversation_summary
-- 作用：存储会话摘要（长期记忆来源），降低历史消息全量回放带来的Token成本。
CREATE TABLE `springagent_conversation_summary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `summary_text` MEDIUMTEXT NOT NULL COMMENT '会话摘要正文',
  `summary_version` INT NOT NULL DEFAULT 1 COMMENT '摘要版本号，便于追踪迭代',
  `source_turn_from` INT NOT NULL COMMENT '本次摘要覆盖的起始轮次',
  `source_turn_to` INT NOT NULL COMMENT '本次摘要覆盖的结束轮次',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SpringAgent三层记忆-会话摘要表（长期记忆）';


-- 表3：springagent_user_profile
-- 作用：存储用户稳定事实（用户画像记忆），如昵称、城市、偏好主题等。
CREATE TABLE `springagent_user_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `fact_key` VARCHAR(64) NOT NULL COMMENT '事实键，例如nickname/location/preference_topic',
  `fact_value` TEXT NOT NULL COMMENT '事实值',
  `confidence` DECIMAL(5,4) NOT NULL DEFAULT 0.8000 COMMENT '置信度，范围0~1',
  `source_conversation_id` VARCHAR(64) DEFAULT NULL COMMENT '事实来源会话ID',
  `last_seen_at` DATETIME DEFAULT NULL COMMENT '最近一次确认该事实的时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_fact` (`user_id`, `fact_key`),
  KEY `idx_user_updated` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SpringAgent三层记忆-用户画像表（稳定事实记忆）';


-- 表4：springagent_memory_job（可选但推荐）
-- 作用：记录摘要生成、画像抽取等记忆任务状态，避免重复执行，便于排障。
CREATE TABLE `springagent_memory_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `job_type` VARCHAR(32) NOT NULL COMMENT '任务类型：SUMMARY/PROFILE_EXTRACT',
  `trigger_turn_no` INT NOT NULL COMMENT '触发任务的轮次',
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/RUNNING/SUCCESS/FAILED',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_job` (`conversation_id`, `job_type`, `trigger_turn_no`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SpringAgent三层记忆-记忆任务状态表（摘要与画像抽取任务跟踪）';
