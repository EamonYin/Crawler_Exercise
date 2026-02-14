package com.crawler.crawler_exercise.service;

import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.input.SpringAIDemoKbInsertInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoKbInsertOutput;

public interface ISpringAIDemoService {
    SpringAIDemoChatOutput demoChat(SpringAIDemoChatInput input);

    SpringAIDemoKbInsertOutput insertKnowledge(SpringAIDemoKbInsertInput input);
}
