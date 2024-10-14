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

//static def fetch2(Task task) {
//    def DPOP = "eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7ImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoiYnc3OExxTC1ORTYzQXVNaDNTMWMyU3F3cFNycERHY1FnczdhRjB1bmNSOCIsInkiOiJpZV9NS0FtcW9IWFRGcVJkaXEzM0NIcWlGLVBHdUcwSzZiUVhDVVJXMjBBIn19.eyJpYXQiOjE3Mjg4MjU2NDksImp0aSI6ImM2NjlkNzIxLTU5YmEtNGEwNi1hYjM2LTk2NWE5M2NjMWVkNiIsImh0dSI6Imh0dHBzOi8vYXBpLm1lcmNhcmkuanAvc2VydmljZXMvbm90aWZpY2F0aW9uL3YxL2dldF91bnJlYWRfY291bnQiLCJodG0iOiJQT1NUIiwidXVpZCI6Ijk0ZDk0YjVlLTY1MjAtNDg5MS1hYWFjLWM4OTRkZWFhZWM2MyJ9.KtXm4EgfFtvDALHwhICr8a4kRC3SdUo_TPPn7cxkTnjJiR6eLXW_Ua0cv0KRgC-eE9W6HGAc3SNjbuXnKfxHFQ"
//    def PROXY_HOST = "127.0.0.1"
//    def PROXY_PROT = 7890
//
//    // https://jp.mercari.com/search?category_id=2635
//    def categoryId = ReUtil.get("category_id=(\\d+)", task.getTaskUrl(), 1) as Integer
//    def result = [:]
//    HttpResponse response = null;
//    try {
//        response = HttpRequest.post("https://api.mercari.jp/v2/entities:search")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
//                .header("Dnt", "1")
//                .header("Dpop", DPOP)
//                .header("origin", "https://jp.mercari.com")
//                .header("priority", "u=1, i")
//                .header("referer", "https://jp.mercari.com/")
//                .header("x-country-code", "JP")
//                .header("x-platform", "web")
//                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
//                .body("{\"userId\":\"\",\"pageSize\":120,\"pageToken\":\"\",\"searchSessionId\":\"42d72376d6671d9bb3ec1c6d8bb1176e\",\"indexRouting\":\"INDEX_ROUTING_UNSPECIFIED\",\"thumbnailTypes\":[],\"searchCondition\":{\"keyword\":\"\",\"excludeKeyword\":\"\",\"sort\":\"SORT_SCORE\",\"order\":\"ORDER_DESC\",\"status\":[],\"sizeId\":[],\"categoryId\":[" + categoryId + "],\"brandId\":[],\"sellerId\":[],\"priceMin\":0,\"priceMax\":0,\"itemConditionId\":[],\"shippingPayerId\":[],\"shippingFromArea\":[],\"shippingMethod\":[],\"colorId\":[],\"hasCoupon\":false,\"attributes\":[],\"itemTypes\":[],\"skuIds\":[],\"shopIds\":[]},\"defaultDatasets\":[\"DATASET_TYPE_MERCARI\",\"DATASET_TYPE_BEYOND\"],\"serviceFrom\":\"suruga\",\"withItemBrand\":true,\"withItemSize\":false,\"withItemPromotions\":true,\"withItemSizes\":true,\"withShopname\":false,\"useDynamicAttribute\":true,\"withSuggestedItems\":true,\"withOfferPricePromotion\":true,\"withProductSuggest\":true,\"withProductArticles\":false,\"withSearchConditionId\":false}")
//                .executeAsync()
//    } catch (Exception e) {
//        throw new RuntimeException("网络请求失败: " + e.getMessage())
//    }
//
//    if (response != null && response.isOk()) {
//        def content = response.body()
//        try {
//            def jsonObj = JSON.parseObject(content)
//            def items = jsonObj.getJSONArray("items")
//            def products = []
//            def tasks = []
//            for (i in 0..<items.size()) {
//                def item = items.getJSONObject(i)
//                def product = [:]
//                def newTask = [:]
//                product["productCode"] = item.getString("id")
//                product["productUrl"] = "https://jp.mercari.com/item/" + product["productCode"]
//                product["productTitle"] = item.getString("name")
//                product["productPrice"] = item.getString("price")
//                def img = []
//                item.getJSONArray("thumbnails").forEach { t ->
//                    img << t
//                }
//                product["productImgUrl"] = img
//                def seller = [:]
//                seller["sellerId"] = item.getString("sellerId")
//                seller["sellerUrl"] = "https://jp.mercari.com/user/profile/" + seller["sellerId"]
//                product["seller"] = seller
//                products << product
//
//                newTask["taskType"] = "mercari-product";
//                newTask["taskUrl"] = product["productUrl"]
//                tasks << newTask
//            }
//            result["tasks"] = tasks
//            result["products"] = products
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

    // https://jp.mercari.com/search?category_id=2635
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
        for (int i = 0; i < 10; i++) {
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
        doc.select("li[data-testid='item-cell'] a").each {
            def url = "https://jp.mercari.com" + it.attr("href")
            def newTask = [:]
            newTask["taskType"] = "mercari-product"
            newTask["taskUrl"] = url
            tasks << newTask
        }

        def next = doc.select("div[data-testid='pagination-next-button'] a")
        if (next.size() > 0) {
            def newTask = [:]
            newTask["taskType"] = "mercari-search"
            newTask["taskUrl"] = "https://jp.mercari.com" + StrUtil.replace(next.get(0).attr("href"), "&amp;", "&", true)
            tasks << newTask
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
    task.setTaskUrl("https://jp.mercari.com/search?category_id=2635")
    println fetch(task)
}