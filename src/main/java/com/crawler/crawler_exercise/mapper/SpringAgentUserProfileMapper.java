package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.SpringAgentUserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 用户画像事实表 Mapper
public interface SpringAgentUserProfileMapper extends BaseMapper<SpringAgentUserProfile> {
}
