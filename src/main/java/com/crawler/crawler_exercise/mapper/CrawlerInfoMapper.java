package com.crawler.crawler_exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrawlerInfoMapper extends BaseMapper<CrawlerInfo> {
}
