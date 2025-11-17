package com.crawler.crawler_exercise.utils.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Cookie;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TOQwen {

    /**
     * 【暂时无法使用】
     * 【暂时无法使用】
     * 【暂时无法使用】
     * 原因：手机号登录的 input无法选中，报错：ElementNotInteractableException: element not interactable
     * 常规 click 非常规 executeScript 都无效
     */

    public void loginInQwen(){
        String url = "https://www.tongyi.com/";
        ChromeDriver chromeDriver = getChromeDriver();
        chromeDriver.get(url);
        // 登录按钮
        WebElement element = chromeDriver.findElement(By.cssSelector(".text-14"));
        element.click();
        chromeDriver.manage().timeouts().implicitlyWait(2L, TimeUnit.SECONDS);
        String pageSource = chromeDriver.getPageSource();
        System.out.println(pageSource);

        WebElement iframe = chromeDriver.findElement(By.xpath("//iframe[contains(@src, 'havanaone/login/login.htm')]"));
        System.out.println("【iframe】："+iframe);
        // 1. 切换到iframe
        chromeDriver.switchTo().frame(iframe);
        System.out.println("【Iframe content】: " + chromeDriver.getPageSource());
        // 输入手机号
        chromeDriver.manage().timeouts().implicitlyWait(20L, TimeUnit.SECONDS);
        WebElement phoneNum = chromeDriver.findElement(By.id("fm-sms-login-id"));
        chromeDriver.executeScript("arguments[0].value = '15620964916'; arguments[0].dispatchEvent(new Event('input'));", phoneNum);
        System.out.println("【phoneNum TExt】:" + phoneNum.getAttribute("value"));
        // 输入验证码
        WebElement phoneCode = chromeDriver.findElement(By.id("fm-smscode"));
        chromeDriver.executeScript("arguments[0].value = '467558'; arguments[0].dispatchEvent(new Event('input'));", phoneCode);
        System.out.println("【phoneCode TExt】:" + phoneCode.getAttribute("value"));

        WebElement loginBtn = chromeDriver.findElement(By.cssSelector(".fm-btn"));
        chromeDriver.executeScript("arguments[0].click();", loginBtn);

//        chromeDriver.close();
    }

    public void getQwenInfo(){
        String url = "https://www.tongyi.com/";
        ChromeDriver chromeDriver = getChromeDriver();
        chromeDriver.get(url);

        // 获取所有cookie
        Set<Cookie> cookies = chromeDriver.manage().getCookies();
        log.info("获取到的cookie数量: {}", cookies.size());

        // 遍历并打印所有cookie
        for (Cookie cookie : cookies) {
            log.info("Cookie名称: {}, 值: {}, 域名: {}", cookie.getName(), cookie.getValue(), cookie.getDomain());
        }

        // 获取特定名称的cookie（示例）
        Cookie specificCookie = chromeDriver.manage().getCookieNamed("someCookieName");
        if (specificCookie != null) {
            log.info("特定Cookie信息 - 名称: {}, 值: {}", specificCookie.getName(), specificCookie.getValue());
        } else {
            log.info("未找到名为 'someCookieName' 的cookie");
        }

        chromeDriver.close();
    }

    @NotNull
    private static ChromeDriver getChromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");

        //设置驱动位置
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver/mac/chromedriver");
        //创建一个谷歌浏览器对象
        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
        return chromeDriver;
    }
}