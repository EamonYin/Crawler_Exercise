package com.crawler.crawler_exercise.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crawler.crawler_exercise.entiy.RankUserInfo;
import com.crawler.crawler_exercise.mapper.RankUserInfoMapper;
import com.crawler.crawler_exercise.service.IRankUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankUserInfoServiceImpl extends ServiceImpl<RankUserInfoMapper, RankUserInfo> implements IRankUserInfoService {
    @Autowired
    private RankUserInfoMapper rankUserInfoMapper;

    @Override
    public RankUserInfo getUserInfoById(String userId) {
        return rankUserInfoMapper.selectById(userId);
    }

    @Override
    public List<RankUserInfo> getUserInfoListByIds(List<String> userIds) {
        return rankUserInfoMapper.selectBatchIds(userIds);
    }
}
