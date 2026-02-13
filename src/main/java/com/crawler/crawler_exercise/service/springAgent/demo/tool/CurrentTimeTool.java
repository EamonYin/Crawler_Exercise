package com.crawler.crawler_exercise.service.springAgent.demo.tool;

import com.crawler.crawler_exercise.service.springAgent.demo.ToolTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class CurrentTimeTool {

    private final ToolTraceContext toolTraceContext;

    public CurrentTimeTool(ToolTraceContext toolTraceContext) {
        this.toolTraceContext = toolTraceContext;
    }

    @Tool(description = "Get current date and time by time zone. Use this tool when user asks about current time/date/today.")
    public String currentTime(@ToolParam(description = "Time zone id, default Asia/Shanghai when empty") String zoneId) {
        String realZoneId = StringUtils.hasText(zoneId) ? zoneId : "Asia/Shanghai";
        ZoneId zid = ZoneId.of(realZoneId);
        String now = ZonedDateTime.now(zid).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        log.info("【Tool触发】current_time 执行完成，zoneId={}, now={}", realZoneId, now);
        toolTraceContext.addTool("current_time");
        toolTraceContext.addSource("system-clock:" + realZoneId);
        return now;
    }
}
