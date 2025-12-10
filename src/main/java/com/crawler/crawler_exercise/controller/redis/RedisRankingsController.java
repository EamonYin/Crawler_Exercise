package com.crawler.crawler_exercise.controller.redis;

import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.entiy.RankUserInfo;
import com.crawler.crawler_exercise.service.IRankUserInfoService;
import com.crawler.crawler_exercise.utils.sse.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rank")
public class RedisRankingsController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IRankUserInfoService rankUserInfoService;
    @Autowired
    private SseEmitterManager sseEmitterManager;

    private final static String RANKING_KEY = "ranking";
    private final static String RELATION = "relation:";

    @GetMapping("/addScore/{uid}/for/{rUid}")
    public String addScore(@PathVariable("uid") Long uid, @PathVariable("rUid") Long rUid) throws Exception {
        String relationKey = RELATION + uid +"-"+ rUid;
        if(redisTemplate.hasKey(relationKey)){
            return "已经点赞过了";
        }else {
            redisTemplate.opsForSet().add(relationKey, String.valueOf(rUid));
            redisTemplate.expire(relationKey, 10, TimeUnit.MINUTES);
        }
        List<RankUserInfo> rank = this.getRank();
        // 开启虚拟线程
        Thread.startVirtualThread(() -> {
            // todo:插入数据库，用于redis重启补偿
            System.out.println("现在时间"+ LocalDate.now()+"--"+rank);
        });
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, String.valueOf(rUid), 1);
        sseEmitterManager.sendMessage("1", JSON.toJSONString(rank), "rank");
        return "点赞成功";
    }

    @GetMapping("/getRank")
    public List<RankUserInfo> getRank() {
        Set<String> ranges = redisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, 10);
        System.out.println(ranges);
        List<String> uidList = new ArrayList<>(ranges);
        System.out.println("转list"+uidList);
        List<RankUserInfo> users = rankUserInfoService.getUserInfoListByIds(uidList);
        // 转 Map（便于按 uid 查）
        Map<String, RankUserInfo> map = users.stream()
                .collect(Collectors.toMap(u -> u.getUserId().toString(), u -> u));

        // 按 Redis 排名顺序组装
        List<RankUserInfo> result = new ArrayList<>();
        for (String uid : uidList) {
            System.out.println("uid"+uid);
            RankUserInfo user = map.get(uid);
            if (user != null) {
                result.add(user);
            }
        }
        return result;
    }


}
