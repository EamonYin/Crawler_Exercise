package com.crawler.crawler_exercise.config;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TOEastmoney {
    public String seleniumProcess() {

        String uri = "http://quote.eastmoney.com/sh600036.html";

        // 设置 chromedirver 的存放位置
        System.getProperties().setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver/mac/chromedriver");

        // 设置浏览器参数
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");//禁用沙箱
        chromeOptions.addArguments("--disable-dev-shm-usage");//禁用开发者shm
        chromeOptions.addArguments("--headless"); //无头浏览器，这样不会打开浏览器窗口
        chromeOptions.addArguments("--remote-allow-origins=*");
        WebDriver webDriver = new ChromeDriver(chromeOptions);

        webDriver.get(uri);
        // 网页上class的形式是有空格的<span class="price_up blinkred">47.20</span>
        WebElement webElements = webDriver.findElement(By.cssSelector(".price_up.blinkred"));
        String stockPrice = webElements.getText();
        System.out.println("最新股价为 >>> {}"+stockPrice);
        webDriver.close();
        return "stockPrice";
    }
}
