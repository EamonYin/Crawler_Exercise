package com.crawler.crawler_exercise.utls.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@Slf4j
public class QwenAPi {
    public String sendSms(String phoneCode, String loginId, String countryCode) throws IOException {
        String SMS_API_URL = "https://passport.tongyi.com/havanaone/loginLegacy/sms/sendSms.do?bizEntrance=tongyi&bizName=tongyi";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(SMS_API_URL);

        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair("phoneCode", phoneCode),
                new BasicNameValuePair("loginId", loginId),
                new BasicNameValuePair("countryCode", countryCode),
                new BasicNameValuePair("defaultView", "sms")
        );

        post.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpResponse response = client.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("qwen-sendSms返回:{}",responseBody);
            try {
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);

                if (!jsonResponse.getBoolean("hasError") &&
                        jsonResponse.getJSONObject("content").getBoolean("success")) {
                    String smsToken = jsonResponse.getJSONObject("content")
                            .getJSONObject("data")
                            .getString("smsToken");
                    if(Objects.isNull(smsToken)){
                        return "";
                    }else {
                        return smsToken;
                    }
                }
            } catch (Exception e) {
                // JSON parsing failed, SMS send unsuccessful
            }

            return null;
        }
    }

    public String login(String phoneCode, String loginId, String countryCode, String smsToken, String smsCode) throws IOException {
        String SMS_LOGIN_API_URL = "https://passport.tongyi.com/havanaone/loginLegacy/sms/login.do?bizEntrance=tongyi&bizName=tongyi";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(SMS_LOGIN_API_URL);

        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair("phoneCode", phoneCode),
                new BasicNameValuePair("loginId", loginId),
                new BasicNameValuePair("countryCode", countryCode),
                new BasicNameValuePair("smsToken", smsToken),
                new BasicNameValuePair("smsCode", smsCode)
        );

        post.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpResponse response = client.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("qwen-login返回:{}",responseBody);
            try {
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);

                if (!jsonResponse.getBoolean("hasError") &&
                        jsonResponse.getJSONObject("content").getBoolean("success")) {

                    return jsonResponse.getJSONObject("content")
                            .getJSONObject("data")
                            .getString("tongyi_sso_ticket");
                }
            } catch (Exception e) {
                // JSON parsing failed, login unsuccessful
            }

            return null;
        }
    }

}