package com.crawler.crawler_exercise.service;

import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.entiy.output.Trip.TripResponse;
import reactor.core.publisher.Flux;

public interface IDashScopeService {
    Flux<String> DashScopeChatByMemory(DashScopeChatInput input);

    TripResponse DashScopeChatByMemoryInStruct(DashScopeChatInput input);

    void DashScopeVoidRead(String message);

    String DashScopeVoiceToText(String musicStr) throws Exception;

    String DashScopeRecordToText(String musicStr);
}