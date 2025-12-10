package com.crawler.crawler_exercise.controller;

import com.alibaba.fastjson.JSON;
import com.crawler.crawler_exercise.utils.sse.SimpleSseManager;
import com.crawler.crawler_exercise.utils.sse.SseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SSEController {

    @Autowired
    private SseEmitterManager sseEmitterManager;
    @Autowired
    private SimpleSseManager simpleSseManager;


    @GetMapping("/sendSSE")
    public void sendSSE(@RequestParam(name = "id") String id, @RequestParam(name = "eventName") String eventName) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("time", LocalDateTime.now().toString());
        map.put("message", "Hello, " + id + " SSE!");
        map.put("id", id);
        String jsonString = JSON.toJSONString(map);
        sseEmitterManager.sendMessage(id, jsonString, eventName);
    }

    @GetMapping("/createEmitter")
    public SseEmitter createEmitter(@RequestParam(name = "id") String id, @RequestParam(name = "eventName") String eventName) {
        return sseEmitterManager.createEmitter(id, eventName);
    }

    // 业务场景1
    String sseKey = "sseKey";
    // 业务场景2
    String sseContent = "sseContent";

    @GetMapping("/sseKey/createSimpleEmitter/{id}")
    public SseEmitter createSimpleEmitterSseKey(@PathVariable("id") String id){
        return simpleSseManager.createEmitter(sseKey+id);
    }

    @GetMapping("/sseContent/createSimpleEmitter")
    public SseEmitter createSimpleEmitterSseContent(){
        return simpleSseManager.createEmitter(sseContent);
    }

    @GetMapping("/sseKey/sendSimpleMessage/{id}")
    public void sendSimpleMessage1(@PathVariable("id") String id, @RequestParam("message") String message) throws Exception {
        simpleSseManager.sendMessage(sseKey+id, message);
    }

    @GetMapping("/sseContent/sendSimpleMessage/{id}")
    public void sendSimpleMessage2(@PathVariable("id") String id, @RequestParam String message) throws Exception {
        simpleSseManager.sendMessage(sseContent, message);
    }

}
