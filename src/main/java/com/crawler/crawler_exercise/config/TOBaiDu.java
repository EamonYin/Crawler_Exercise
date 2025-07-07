package com.crawler.crawler_exercise.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TOBaiDu {
    public String crawlerBaidu(){
        // 设置浏览器参数
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");

        //设置驱动位置
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver/mac/chromedriver");
        //创建一个谷歌浏览器对象
        WebDriver driver = new ChromeDriver(chromeOptions);    //Chrome浏览器
        //访问百度首页
        driver.get("https://www.baidu.com/");

        //获取标题，并打印
        System.out.println("百度的标题是:"+driver.getTitle());
        String result = driver.getTitle();

        //关闭浏览器
        driver.close();
        return result;
    }
}
