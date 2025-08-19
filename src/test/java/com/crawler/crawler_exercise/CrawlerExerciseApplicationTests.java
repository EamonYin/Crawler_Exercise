package com.crawler.crawler_exercise;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crawler.crawler_exercise.entiy.CrawlerInfo;
import com.crawler.crawler_exercise.service.ICrawlerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class CrawlerExerciseApplicationTests {

    @Resource
    ICrawlerInfoService crawlerInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void mysqlTest() {
        CrawlerInfo crawlerInfo = new CrawlerInfo();
        crawlerInfo.setInfo("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>测试 MEDIUMTEXT 数据</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>这是一篇测试文章</h1>\n" +
                "    \n" +
                "    <p>这是一段普通文本，用于测试 MEDIUMTEXT 类型的存储能力。MEDIUMTEXT 最大可存储约 16MB 的内容，足以容纳长篇文章、富文本编辑内容、日志记录等。</p>\n" +
                "    \n" +
                "    <h2>1. 格式化内容示例</h2>\n" +
                "    <p>包含<strong>加粗文本</strong>、<em>斜体文本</em>、<u>下划线文本</u>和<del>删除线文本</del>。</p>\n" +
                "    \n" +
                "    <h2>2. 列表示例</h2>\n" +
                "    <ul>\n" +
                "        <li>无序列表项 1</li>\n" +
                "        <li>无序列表项 2</li>\n" +
                "        <li>无序列表项 3</li>\n" +
                "    </ul>\n" +
                "    <ol>\n" +
                "        <li>有序列表项 1</li>\n" +
                "        <li>有序列表项 2</li>\n" +
                "        <li>有序列表项 3</li>\n" +
                "    </ol>\n" +
                "    \n" +
                "    <h2>3. 代码块示例</h2>\n" +
                "    <pre><code>public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        // 这是一段Java代码示例\n" +
                "        String mediumText = \"用于测试MEDIUMTEXT类型的字符串内容\";\n" +
                "        System.out.println(\"内容长度：\" + mediumText.length());\n" +
                "    }\n" +
                "}</code></pre>\n" +
                "    \n" +
                "    <h2>4. 表格示例</h2>\n" +
                "    <table border=\"1\">\n" +
                "        <tr>\n" +
                "            <th>表头1</th>\n" +
                "            <th>表头2</th>\n" +
                "            <th>表头3</th>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>单元格1</td>\n" +
                "            <td>单元格2</td>\n" +
                "            <td>单元格3</td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "    \n" +
                "    <h2>5. 重复内容（用于测试长度）</h2>\n" +
                "    <p>\n" +
                "        重复文本开始： Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "        <!-- 可以根据需要复制上述段落多次，以接近 MEDIUMTEXT 的容量上限 -->\n" +
                "        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "    </p>\n" +
                "    \n" +
                "    <div style=\"color: #666; margin-top: 20px;\">\n" +
                "        <p>测试数据结束 | 最后更新时间：2025-08-12</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>");
        crawlerInfoService.insertCrawlerInfo(crawlerInfo);

    }

    @Test
    void mysqlGetTest() {
        List<CrawlerInfo> list = crawlerInfoService.list(new QueryWrapper<CrawlerInfo>());
        log.info("查询数据库中的数据:{}", JSON.toJSONString(list));
    }

    @Test
    void testRedis() {
        // 写入数据
        redisTemplate.opsForValue().set("test_key", "Hello Spring Boot Redis!");

        // 读取数据
        String value = redisTemplate.opsForValue().get("test_key");

        System.out.println("Redis value: " + value);
    }

}
