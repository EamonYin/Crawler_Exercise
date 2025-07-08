package com.crawler.crawler_exercise.config;

import com.crawler.crawler_exercise.entiy.V2EXInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TOV2EX {
    public List<V2EXInfo> getV2EXInfo() {

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
        for (int i = 1; i < elements.size(); i++) {
            V2EXInfo v2EXInfo = new V2EXInfo();
            // 获取内层元素
            WebElement titleElement = elements.get(i).findElement(By.cssSelector(".item_title"));
            String title = titleElement.getText();
            v2EXInfo.setSort(i);
            v2EXInfo.setTitle(title);
            titleLst.add(v2EXInfo);
//            System.out.println("getV2EXInfo输出内容:【" + title+"】");
        }
        chromeDriver.close();
        return titleLst;

    }
}
