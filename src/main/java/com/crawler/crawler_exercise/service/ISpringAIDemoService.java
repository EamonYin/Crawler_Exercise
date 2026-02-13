package com.crawler.crawler_exercise.service;

import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;

public interface ISpringAIDemoService {
    SpringAIDemoChatOutput demoChat(SpringAIDemoChatInput input);
}
