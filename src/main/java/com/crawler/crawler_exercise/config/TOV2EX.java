package com.crawler.crawler_exercise.config;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
@Slf4j
public class TOV2EX {
    public String getV2EXInfo() {

        String url = "https://www.v2ex.com/";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");

        //设置驱动位置
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver/mac/chromedriver");
        //创建一个谷歌浏览器对象
        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
        chromeDriver.get(url);

        String currentUrl = chromeDriver.getCurrentUrl();
        log.info("getV2EXInfo输出内容:{}", currentUrl);

        chromeDriver.close();

        return currentUrl;

    }
}
