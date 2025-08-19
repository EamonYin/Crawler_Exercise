package com.crawler.crawler_exercise.entiy;

import lombok.Data;

@Data
public class QwenMsg {
    private Long id;
    private String phoneCode;
    private String loginId;
    private String countryCode;
    private String smsToken;
    private String smsCode;
    private String tongyiSsoTicket;
    private Integer deFlg;
}
