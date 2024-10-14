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

//static def fetch2(Task task) {
//    def DPOP = "eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7ImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoiYnc3OExxTC1ORTYzQXVNaDNTMWMyU3F3cFNycERHY1FnczdhRjB1bmNSOCIsInkiOiJpZV9NS0FtcW9IWFRGcVJkaXEzM0NIcWlGLVBHdUcwSzZiUVhDVVJXMjBBIn19.eyJpYXQiOjE3Mjg4MjU2NDksImp0aSI6ImM2NjlkNzIxLTU5YmEtNGEwNi1hYjM2LTk2NWE5M2NjMWVkNiIsImh0dSI6Imh0dHBzOi8vYXBpLm1lcmNhcmkuanAvc2VydmljZXMvbm90aWZpY2F0aW9uL3YxL2dldF91bnJlYWRfY291bnQiLCJodG0iOiJQT1NUIiwidXVpZCI6Ijk0ZDk0YjVlLTY1MjAtNDg5MS1hYWFjLWM4OTRkZWFhZWM2MyJ9.KtXm4EgfFtvDALHwhICr8a4kRC3SdUo_TPPn7cxkTnjJiR6eLXW_Ua0cv0KRgC-eE9W6HGAc3SNjbuXnKfxHFQ"
//    def IMG_PATH = "D:/Workspaces/go-crawler/uploads/productImg/mercari/"
//    def PROXY_HOST = "127.0.0.1"
//    def PROXY_PROT = 7890
//
//    // https://jp.mercari.com/item/m66274034354
//    def productId = StrUtil.subAfter(task.getTaskUrl(), "/", true)
//    def result = [:]
//    HttpResponse response = null;
//    try {
//        response = HttpRequest.get("https://api.mercari.jp/items/get?id=" + productId + "&include_item_attributes=true&include_product_page_component=true&include_non_ui_item_attributes=true&include_donation=true&include_offer_like_coupon_display=true&include_offer_coupon_display=true")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
//                .header("Dnt", "1")
//                .header("Dpop", DPOP)
//                .header("origin", "https://jp.mercari.com")
//                .header("priority", "u=1, i")
//                .header("referer", "https://jp.mercari.com/")
//                .header("x-platform", "web")
//                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
//                .executeAsync()
//    } catch (Exception e) {
//        throw new RuntimeException("网络请求失败: " + e.getMessage())
//    }
//
//    if (response != null && response.isOk()) {
//        def content = response.body()
//        try {
//            def jsonObj = JSON.parseObject(content)
//            def item = jsonObj.getJSONObject("data")
//            def product = [:]
//            product["productSite"] = "mercari"
//            product["productCode"] = item.getString("id")
//            product["productTitle"] = item.getString("name")
//            product["productPrice"] = item.getString("price")
//            product["productUrl"] = "https://jp.mercari.com/item/" + product["productCode"]
//            def img = []
//            item.getJSONArray("photos").forEach { t ->
//                {
//                    def imgUrl = StrUtil.subBefore(t as String, "?", true)
//                    img << imgUrl
//                    ImgDownloader.download(imgUrl, IMG_PATH + product["productCode"] + "/")
//                }
//            }
//            product["productMainImg"] = img[0]
//            product["productThumbnail"] = img
//            product["productDesc"] = item.getString("description")
//
//            def categories = []
//            item.getJSONArray("parent_categories_ntiers").forEach { t ->
//                {
//                    def ntier = t as JSONObject
//                    def category = [:]
//                    category["categoryName"] = ntier.getString("name")
//                    category["categoryId"] = ntier.getString("id")
//                    category["categoryUrl"] = "https://jp.mercari.com/search?category_id=" + category["categoryId"]
//                    categories << category
//                }
//            }
//            product["category"] = categories
//
//            def seller = [:]
//            seller["sellerName"] = item.getJSONObject("seller").getString("name")
//            seller["sellerId"] = item.getJSONObject("seller").getString("id")
//            seller["sellerUrl"] = "https://jp.mercari.com/user/profile/" + seller["sellerId"]
//            product["seller"] = seller
//
//            result["product"] = product
//            task.setTaskStatus(TaskStatusEnum.FINISHED.getCode());
//            return JSON.toJSONString(result)
//        } catch (Exception e) {
//            throw new RuntimeException("结果解析失败: " + e.getMessage())
//        }
//    } else {
//        String resson = response == null ? "response is null" : response.getStatus() + ""
//        throw new RuntimeException("网络请求失败: " + resson)
//    }
//}

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
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[data-testid='name']")));
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
        product["productPrice"] = doc.select("meta[name='product:price:amount']").attr("content")
        def img = doc.select("meta[property='og:image']").attr("content")
        product["productMainImg"] = StrUtil.subBefore(img, "?", true)

        product["productDesc"] = doc.select("pre[data-testid='description']").text()

        def categories = []
        doc.select("div[data-testid='item-detail-category'] a").each {
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
    task.setTaskUrl("https://jp.mercari.com/item/m76452049891")
    println fetch(task)
}