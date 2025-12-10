package com.crawler.crawler_exercise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crawler.crawler_exercise.entiy.RankUserInfo;

import java.util.List;

public interface IRankUserInfoService extends IService<RankUserInfo> {

    RankUserInfo getUserInfoById(String userId);
    List<RankUserInfo> getUserInfoListByIds(List<String> userIds);

}
