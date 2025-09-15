package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.service.IDashScopeService;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/dashScope")
@Slf4j
public class DashScopeController {

    @Autowired
    private IDashScopeService dashScopeService;

    @PostMapping(value = "/chatV2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatV2(@RequestBody DashScopeChatInput input){
       return dashScopeService.DashScopeChatByMemory(input);
    }

}
