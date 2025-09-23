package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.entiy.output.Trip.TripResponse;
import com.crawler.crawler_exercise.service.IDashScopeService;
import lombok.extern.slf4j.Slf4j;
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
       return dashScopeService.dashScopeChatByMemory(input);
    }

    @PostMapping(value = "/chatInStruct")
    public TripResponse chatInStruct(@RequestBody DashScopeChatInput input){
        return dashScopeService.dashScopeChatByMemoryInStruct(input);
    }

    @GetMapping(value = "/chatInVoice")
    public String chatInVoice(@RequestParam("questionStr") String questionStr) {
        return dashScopeService.dashScopeVoidRead(questionStr);
    }


    @GetMapping(value = "/chatInVoiceToText")
    public String chatVoiceToText(@RequestParam("musicUrl") String musicUrl) throws Exception {
        return dashScopeService.dashScopeVoiceToText(musicUrl);
    }

    // OSS录音在线转文字
    @GetMapping(value = "/DashScopeOnlineVoiceToText")
    public String DashScopeOnlineVoiceToText(@RequestParam("musicUrl") String musicUrl) throws Exception {
        return dashScopeService.dashScopeOnlineVoiceToText(musicUrl);
    }

    @GetMapping(value = "/recordToText")
    public void recordToText (@RequestParam("musicUrl") String musicUrl){
        dashScopeService.dashScopeRecordToText(musicUrl);
    }
}
