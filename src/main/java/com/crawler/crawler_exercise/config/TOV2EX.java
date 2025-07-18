package com.crawler.crawler_exercise.config;

import com.crawler.crawler_exercise.entiy.V2EXInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TOV2EX {
    public List<V2EXInfo> getV2EXInfo() throws InterruptedException {

        String url = "https://www.v2ex.com/";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");

        //设置驱动位置
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver/mac/chromedriver");
        //创建一个谷歌浏览器对象
        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
        chromeDriver.get(url);
        List<V2EXInfo> titleLst = new ArrayList<>();
        // 获取外层元素
        List<WebElement> elements = chromeDriver.findElements(By.cssSelector(".cell.item"));
        for (int i = 0; i < elements.size(); i++) {
            V2EXInfo v2EXInfo = new V2EXInfo();
            // 获取内层元素
            WebElement v2EXElement = elements.get(i);
            WebElement titleElement = v2EXElement.findElement(By.cssSelector(".item_title"));
            String title = titleElement.getText();
            // 点击
            titleElement.click();
            log.info("[点击]新页面地址:{}", chromeDriver.getCurrentUrl());
            log.info("[点击]新页面的标题:{}", chromeDriver.getTitle());
//            Thread.sleep(30);
            chromeDriver.manage().timeouts().implicitlyWait(2L, TimeUnit.SECONDS);
            // 获取新页面元素
            try{
                WebElement newPageElement = chromeDriver.findElement(By.cssSelector(".topic_content"));
                log.info("[点击]新页面的内容:{}", newPageElement.getText());
                v2EXInfo.setInfo(newPageElement.getText());
            }catch (Exception e){
                log.warn("未找到 .topic_content 元素，跳过此条记录");
                // 可选：设置默认值或跳过当前循环
                v2EXInfo.setInfo("内容未找到");
            }
            // 获取内容
            v2EXInfo.setSort(i);
            v2EXInfo.setTitle(title);
            titleLst.add(v2EXInfo);
//            Thread.sleep(10);
            chromeDriver.navigate().back();
            log.info("[返回]新页面的链接:{}", chromeDriver.getCurrentUrl());
            //重新赋值
//            elements = chromeDriver.findElements(By.cssSelector(".cell.item"));
        }
        chromeDriver.close();
        return titleLst;

    }
}
