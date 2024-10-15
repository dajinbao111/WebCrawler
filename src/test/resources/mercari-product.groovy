import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson2.JSON
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.nio.file.Paths
import java.time.Duration

static def fetch(Task task) {
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    // https://jp.mercari.com/item/m66274034354
    def result = [:]

    WebDriver driver = null;
    try {
        ChromeOptions options = new ChromeOptions()
        options.addArguments("--proxy-server=socks5://" + PROXY_HOST + ":" + PROXY_PROT)
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
        options.setCapability("se:downloadsEnabled", true);
        options.addArguments("--user-data-dir=C:/Users/Administrator/AppData/Local/Google/Chrome/User Data/Default")
        driver = new ChromeDriver(options)
        driver.get(task.getTaskUrl())
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        if (StrUtil.contains(task.getTaskUrl(), "/shops/product/")) {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[data-testid='display-name']")));
        } else if (StrUtil.contains(task.getTaskUrl(), "/item/")) {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[data-testid='name']")));
        }
    } catch (Exception e) {
        if (driver != null) {
            driver.quit()
        }
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    String source = driver.getPageSource()

    def product = [:]
    product["productSite"] = "mercari"

    try {
        def doc = Jsoup.parse(source)
        product["productTitle"] = doc.select("meta[name='twitter:title']").attr("content")
        product["productUrl"] = doc.select("meta[property='og:url']").attr("content")
        product["productCode"] = StrUtil.subAfter(product["productUrl"] as CharSequence, "/", true)
        def el = doc.select("meta[name='product:price:amount']")
        if (!el.isEmpty()) {
            product["productPrice"] = doc.select("meta[name='product:price:amount']").attr("content")
        } else {
            product["productPrice"] = StrUtil.replace(doc.select("div[data-testid='product-price']").text(), "¥", "")
        }
        def img = doc.select("meta[property='og:image']").attr("content")
        product["productMainImg"] = StrUtil.subBefore(img, "?", true)
        product["productMainImg"] = StrUtil.subBefore(product["productMainImg"] as String, "@", true)

        product["productDesc"] = doc.select("pre[data-testid='description']").text()

        def categories = []
        doc.select("div[data-testid\$='-detail-category'] a").each {
            def category = [:]
            category["categoryName"] = it.text()
            category["categoryUrl"] = "https://jp.mercari.com" + it.attr("href")
            category["categoryId"] = ReUtil.get("category_id=(\\d+)", category["categoryUrl"] as String, 1)
            categories << category
        }
        product["category"] = categories

        def tasks = []
        def newTask = [:]
        newTask["taskType"] = "mercari-image"
        newTask["taskUrl"] = product["productMainImg"]
        tasks << newTask
        result["tasks"] = tasks
        result["product"] = product
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
//    task.setTaskUrl("https://jp.mercari.com/item/m76452049891")
    task.setTaskUrl("https://jp.mercari.com/shops/product/mebNqsMMy72Pr5gz7RhWt5")
    println fetch(task)
}