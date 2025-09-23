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

    @PostMapping(value = "/chatInVoice")
    public void chatInVoice() {
        dashScopeService.dashScopeVoidRead("你好呀！");
    }


    @PostMapping(value = "/chatInVoiceToText")
    public String chatVoiceToText() throws Exception {
        return dashScopeService.dashScopeVoiceToText("https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav");
    }

    // OSS录音在线转文字
    @PostMapping(value = "/DashScopeOnlineVoiceToText")
    public String DashScopeOnlineVoiceToText() throws Exception {
        return dashScopeService.dashScopeOnlineVoiceToText("https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav");
    }

    @PostMapping(value = "/recordToText")
    public void recordToText (){
        dashScopeService.dashScopeRecordToText("");
    }
}
