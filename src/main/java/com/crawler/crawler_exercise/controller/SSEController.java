package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.utils.sse.SseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SSEController {

    @Autowired
    private SseEmitterManager sseEmitterManager;


    @GetMapping("/sendSSE")
    public void sendSSE(@RequestParam(name = "id") String id, @RequestParam(name = "eventName") String eventName) throws Exception {
        sseEmitterManager.sendMessage(id, "hello " + id + "now is" + LocalDateTime.now(), eventName);
    }

    @GetMapping("/createEmitter")
    public SseEmitter createEmitter(@RequestParam(name = "id") String id, @RequestParam(name = "eventName") String eventName) {
        return sseEmitterManager.createEmitter(id, eventName);
    }

}
