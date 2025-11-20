package com.crawler.crawler_exercise.controller;

import com.crawler.crawler_exercise.utils.websocket.WebSocketServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/socket")
public class WebSocketController {
    //页面请求（请求地址：ws://127.0.0.1:8083/api/websocket/100）
    @GetMapping("/index/{userId}")
    public ModelAndView socket(@PathVariable String userId) {
        ModelAndView mav = new ModelAndView("/socket1");
        mav.addObject("userId", userId);
        return mav;
    }

    //推送数据接口（请求地址：127.0.0.1:8083/api/socket/push?cid=101&message=谢谢）
    @GetMapping("/push")
    public Map pushToWeb(@RequestParam("cid") String cid, @RequestParam("message") String message) {
        Map<String,Object> result = new HashMap<>();
        try {
            WebSocketServer.sendInfo(message, cid);
            result.put("code", cid);
            result.put("msg", message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
