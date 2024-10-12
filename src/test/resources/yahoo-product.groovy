import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson2.JSON
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task
import com.github.dajinbao.crawler.ImgDownloader
import org.jsoup.Jsoup

static def fetch(Task task) {
    def IMG_PATH = "D:/Workspaces/go-crawler/uploads/productImg/yahoo/"
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    def result = [:]
    HttpResponse response = null;
    try {
        response = HttpRequest.get(task.getTaskUrl())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .header("Dnt", "1")
                .header("priority", "u=0, i")
                .header("referer", "https://auctions.yahoo.co.jp/")
                .header("upgrade-insecure-requests", "1")
                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
                .setFollowRedirects(true)
                .executeAsync()
    } catch (Exception e) {
        task.setTaskStatus(TaskStatusEnum.FAILED.getCode())
        task.setFailReason("网络请求失败: " + task.getTaskType())
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def product = [:]

        def doc = Jsoup.parse(response.body());
        product["productTitle"] = doc.select("div.ProductTitle > div.ProductTitle__title > .ProductTitle__text").get(0).text()
        product["productUrl"] = doc.select("head > meta[property='og:url']").attr("content")
        product["productCode"] = StrUtil.subAfter(product["productUrl"] as CharSequence, "/", true)

        def prices = []
        doc.select("div.Price__borderBox > .Price__body > div.Price__row").each {
            def price = [:]
            price[it.selectFirst("dt.Price__title").text()] = it.selectFirst("dd.Price__value").text()
            prices << price
        }
        product["productPrice"] = prices

        def img = []
        doc.select("div.ProductImage > div.ProductImage__body > ul.ProductImage__images > li.ProductImage__image").each {
            def imgUrl = it.selectFirst("div.ProductImage__inner > img").attr("src")
            img << imgUrl
            ImgDownloader.download(imgUrl, IMG_PATH + product["productCode"] + "/")
        }
        product["productImgUrl"] = img

        def info = [:]
        doc.select("li.ProductInformation__item table.Section__table tr.Section__tableRow").each {
            info[it.selectFirst("th.Section__tableHead").text()] = it.selectFirst("td.Section__tableData").text()
        }
        product["productInfo"] = info
        product["productDesc"] = doc.select("div.ProductExplanation div.ProductExplanation__commentBody").html().trim()

        def categories = []
        doc.select("div#yjBreadcrumbs a[data-cl-params~=catid:(\\d+)]").each {
            def category = [:]
            category["categoryName"] = it.text()
            category["categoryUrl"] = it.attr("href")
            category["categoryId"] = ReUtil.get("catid:(\\d+)", it.attr("data-cl-params"), 1)
            categories << category
        }
        product["category"] = categories

        def seller = [:]
        seller["sellerName"] = doc.select("div.Seller div.Seller__info p.Seller__name a").text()
        seller["sellerUrl"] = doc.select("div.Seller div.Seller__info p.Seller__name a").attr("href")
        seller["sellerId"] = StrUtil.subAfter(seller["sellerUrl"] as CharSequence, "/", true)
        product["seller"] = seller

        result["product"] = product
        task.setTaskStatus(TaskStatusEnum.FINISHED.getCode());
        return JSON.toJSONString(result)
    } else {
        String resson = response == null ? "response is null" : response.getStatus() + ""
        task.setTaskStatus(TaskStatusEnum.FAILED.getCode())
        task.setFailReason("网络请求失败: " + task.getTaskType())
        throw new RuntimeException("网络请求失败: " + resson)
    }
}

static void main(String[] args) {
    def task = new Task()
    task.setTaskUrl("https://page.auctions.yahoo.co.jp/jp/auction/b1156096151")
    println fetch(task)
}