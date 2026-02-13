package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.entiy.input.SpringAIDemoChatInput;
import com.crawler.crawler_exercise.entiy.output.SpringAIDemoChatOutput;
import com.crawler.crawler_exercise.service.ISpringAIDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/springAi")
@Slf4j
public class SpringAgentController {

    @Autowired
    private ISpringAIDemoService springAIDemoService;

    @PostMapping("/agent/demoChat")
    public SpringAIDemoChatOutput demoChat(@RequestBody SpringAIDemoChatInput input) {
        return springAIDemoService.demoChat(input);
    }
}
