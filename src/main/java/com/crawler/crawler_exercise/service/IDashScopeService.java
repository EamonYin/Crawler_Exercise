package com.crawler.crawler_exercise.service;

import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import reactor.core.publisher.Flux;

public interface IDashScopeService {
    Flux<String> DashScopeChatByMemory(DashScopeChatInput input);
}