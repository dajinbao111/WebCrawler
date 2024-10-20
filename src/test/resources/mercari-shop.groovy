import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson2.JSON
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task
import org.jsoup.Jsoup
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

import java.util.concurrent.TimeUnit

static def fetch(Task task) {
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    // https://mercari-shops.com/shops/iWjdESi7Hj6YVHtTQiEbNC
    def result = [:]

    WebDriver driver = null;
    try {
        ChromeOptions options = new ChromeOptions()
        options.addArguments("--proxy-server=socks5://" + PROXY_HOST + ":" + PROXY_PROT)
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
        options.addArguments("--user-data-dir=C:/Users/Administrator/AppData/Local/Google/Chrome/User Data/Default")
        driver = new ChromeDriver(options)
        driver.get(task.getTaskUrl())
        JavascriptExecutor js = (JavascriptExecutor) driver
        for (int i = 0; i < 20; i++) {
            js.executeScript("window.scrollTo(" + (i * 500) + ", " + ((i + 1) * 500) + ")")
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    } catch (Exception e) {
        if (driver != null) {
            driver.quit()
        }
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    String source = driver.getPageSource()

    try {
        def doc = Jsoup.parse(source)

        def tasks = []
        doc.select("a[data-testid='seller-link']").each {
            def el = it.select("div[data-testid='products']")
            if (!el.isEmpty()) {
                def url = StrUtil.subBefore(it.attr("href"), "?", true)
                def newTask = [:]
                newTask["taskType"] = "mercari-product"
                newTask["taskUrl"] = url
                tasks << newTask
            }
        }

        result["tasks"] = tasks
        task.setTaskStatus(TaskStatusEnum.FINISHED.getCode());
        return JSON.toJSONString(result)
    } catch (Exception e) {
        throw new RuntimeException("结果解析失败: " + e.getMessage())
    } finally {
        if (driver != null) {
            driver.quit()
        }
    }
}

static void main(String[] args) {
    def task = new Task()
    task.setTaskUrl("https://mercari-shops.com/shops/iWjdESi7Hj6YVHtTQiEbNC")
    println fetch(task)
}