package com.github.dajinbao.apiserver.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;



public class WebDriverTest {

    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--proxy-server=socks5://127.0.0.1:7890");
//        options.addArguments("--headless");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36");
        options.addExtensions(new File("C:/Users/Administrator/Downloads/1688-extension.crx"));
        options.addArguments("--user-data-dir=C:/Users/Administrator/AppData/Local/Google/Chrome/User Data/Default");
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.1688.com");
//        driver.get("chrome-extension://dgeafmgkobkeendinpademeoiaagknoj/popup.html");
//        driver.get("https://jp.mercari.com/search?category_id=1184&brand_id=3441");
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        for (int i = 0; i < 10; i++) {
//            js.executeScript("window.scrollTo(" + (i * 500) + ", " + ((i + 1) * 500) + ")");
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.image-input-button")));
        driver.findElement(By.cssSelector("div.image-input-button")).click();
//        WebElement searchByCamera = driver.findElement(By.cssSelector("div.btn-camera"));
//        searchByCamera.click();
        String source = driver.getPageSource();
        System.out.println(source);
//        driver.quit();
    }

}
