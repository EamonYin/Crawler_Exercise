package com.crawler.crawler_exercise.service;

import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.entiy.output.Trip.TripResponse;
import reactor.core.publisher.Flux;

public interface IDashScopeService {
    Flux<String> dashScopeChatByMemory(DashScopeChatInput input);

    TripResponse dashScopeChatByMemoryInStruct(DashScopeChatInput input);

    String dashScopeVoidRead(String message);

    String dashScopeVoiceToText(String musicStr) throws Exception;

    String dashScopeOnlineVoiceToText(String musicStr) throws Exception;

    String dashScopeRecordToText(String musicStr);
}