package com.crawler.crawler_exercise.controller.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sign")
public class RedisBitMapController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 使用redis的bitmap实现签到功能
     */
    @GetMapping("/sign")
    public boolean sign(Long userId) {
        String key = buildSignKey(userId);
        int offset = getDayOffset();

        Boolean result = redisTemplate.opsForValue().setBit(key, offset, true);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 查看特定日期是否签到
     */
    @GetMapping("/isSign")
    public boolean isSigned(Long userId, LocalDate date) {
        String key = "user:sign:" + userId + ":" + date.getYear() + date.getMonthValue();
        int offset = date.getDayOfMonth() - 1;

        Boolean bit = redisTemplate.opsForValue().getBit(key, offset);
        return Boolean.TRUE.equals(bit);
    }

    @GetMapping("/getMonthSignInfoList")
    public void getMonthSignInfoList(Long userId) {
        String key = buildSignKey(userId);
        // 1. 先准备 Redis 的 bitfield 子命令对象：从 offset=0 开始，取一个无符号的 31 位整数（最多 31 天）
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                // 声明我们要取的字段类型：无符号 31 位（u31）
                .get(BitFieldSubCommands.BitFieldType.unsigned(31))
                // 从第 0 位开始取（也就是当月第 1 天，对应的 offset=0）
                .valueAt(0);

        // 2. 使用 StringRedisTemplate 执行底层 RedisCallback，拿到 bitfield 的结果
        List<Long> result = redisTemplate.execute(
                // 这是一个匿名内部类 / Lambda，类型是 RedisCallback<List<Long>>
                (RedisCallback<List<Long>>) connection -> {
                    // 2.1 把 key 转成 byte[]，因为底层 RedisConnection 接口用的是二进制
                    byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);

                    // 2.2 调用底层的 bitField 命令，传入 key 和我们构造好的子命令
                    List<Long> bitFieldResult = connection.bitField(rawKey, bitFieldSubCommands);

                    // 2.3 把结果返回给外层的 execute，最终赋值给 result
                    return bitFieldResult;
                }
        );
        System.out.println(result);
    }

    private String buildSignKey(Long userId) {
        LocalDate now = LocalDate.now();
        return "user:sign:" + userId + ":" + now.getYear() + now.getMonthValue();
    }

    private int getDayOffset() {
        return LocalDate.now().getDayOfMonth() - 1;
    }
}
