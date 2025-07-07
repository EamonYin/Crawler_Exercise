package com.crawler.crawler_exercise.controller;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CrawlerController {
    public static void main(String[] args) {
//        CrawlerController crawlerController = new CrawlerController();
//        crawlerController.seleniumProcess();

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
        System.out.println("哈哈:"+driver.getTitle());
        //关闭浏览器
        driver.close();

    }


    private void seleniumProcess() {

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
        WebElement webElements = webDriver.findElement(By.id("price9"));
        String stockPrice = webElements.getText();
        System.out.println("最新股价为 >>> {}"+stockPrice);
        webDriver.close();
    }
}
