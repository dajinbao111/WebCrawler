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
    def IMG_PATH = "c:/webcrawler/uploads/productImg/yahoo/"
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
        task.setFailReason("网络请求失败: " + e.getMessage())
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def product = [:]
        product["productSite"] = "yahoo"

        try {
            def doc = Jsoup.parse(response.body())
            def el = doc.select("div.ProductTitle > div.ProductTitle__title > .ProductTitle__text")
            if (!el.isEmpty()) {
                product["productTitle"] = el.get(0).text()
            }
            el = doc.select("div[class^=styles_itemName] p[class^=styles_name]")
            if (!el.isEmpty()) {
                product["productTitle"] = el.get(0).text()
            }
            product["productUrl"] = doc.select("head > meta[property='og:url']").attr("content")
            product["productCode"] = StrUtil.subAfter(product["productUrl"] as CharSequence, "/", true)
            product["productCode"] = StrUtil.subBefore(product["productCode"] as String, ".html", true)

            el = doc.select("div[class^=styles_priceArea] p[class^=styles_price]")
            if (!el.isEmpty()) {
                product["productPrice"] = el.get(0).text()
            } else {
                def prices = []
                doc.select("div.Price__borderBox > .Price__body > div.Price__row").each {
                    prices << it.selectFirst("dd.Price__value").text()
                }
                product["productPrice"] = prices[0]
            }

            def img = []
            el = doc.select("ul[class^=styles_thumbnailItems] li[class^=styles_thumbnailItem] img[class^=styles_thumbnailImage]")
            if (!el.isEmpty()) {
                doc.select("ul[class^=styles_thumbnailItems] li[class^=styles_thumbnailItem] img[class^=styles_thumbnailImage]").each {
                    def imgUrl = it.attr("src")
                    if (StrUtil.startWith(imgUrl, "http")) {
                        imgUrl = StrUtil.subBefore(imgUrl, "?", true)
                        img << imgUrl
                        ImgDownloader.download(imgUrl, IMG_PATH + product["productCode"] + "/")
                    }
                }
                product["productMainImg"] = img[0]
                product["productThumbnail"] = img
            } else {
                doc.select("div.ProductImage > div.ProductImage__body > ul.ProductImage__images > li.ProductImage__image").each {
                    def imgUrl = it.selectFirst("div.ProductImage__inner > img").attr("src")
                    img << imgUrl
                    ImgDownloader.download(imgUrl, IMG_PATH + product["productCode"] + "/")
                }
                product["productMainImg"] = img[0]
                product["productThumbnail"] = img
            }

            def info = [:]
            doc.select("li.ProductInformation__item table.Section__table tr.Section__tableRow").each {
                info[it.selectFirst("th.Section__tableHead").text()] = it.selectFirst("td.Section__tableData").text()
            }
            product["productInfo"] = info

            el = doc.select("div.ProductExplanation div.ProductExplanation__commentBody")
            if (!el.isEmpty()) {
                product["productDesc"] = el.html().trim()
            } else {
                product["productDesc"] = doc.select("div[data-testid=information]").html().trim()
                product["productDesc"] += doc.select("div[data-testid=freeSpace] script").html().trim()
            }

            def categories = []
            el = doc.select("div#yjBreadcrumbs a[data-cl-params~=catid:(\\d+)]")
            if (!el.isEmpty()) {
                doc.select("div#yjBreadcrumbs a[data-cl-params~=catid:(\\d+)]").each {
                    def category = [:]
                    category["categoryName"] = it.text()
                    category["categoryUrl"] = it.attr("href")
                    category["categoryId"] = ReUtil.get("catid:(\\d+)", it.attr("data-cl-params"), 1)
                    categories << category
                }
            } else {
                doc.select("div.Breadcrumb ul.Breadcrumb__list").get(2).select("li.Breadcrumb__item a").each {
                    def category = [:]
                    category["categoryName"] = it.text()
                    category["categoryUrl"] = it.attr("href")
                    categories << category
                }
            }
            product["category"] = categories


            def seller = [:]
            el = doc.select("div[class^=styles_storeInfoWrap]")
            if (!el.isEmpty()) {
                seller["sellerName"] = el.select("p").text()
                seller["sellerUrl"] = el.select("a").attr("href")
            } else {
                seller["sellerName"] = doc.select("div.Seller div.Seller__info p.Seller__name a").text()
                seller["sellerUrl"] = doc.select("div.Seller div.Seller__info p.Seller__name a").attr("href")
                seller["sellerId"] = StrUtil.subAfter(seller["sellerUrl"] as CharSequence, "/", true)
            }
            product["seller"] = seller


            result["product"] = product
            task.setTaskStatus(TaskStatusEnum.FINISHED.getCode());
            return JSON.toJSONString(result)
        } catch (Exception e) {
            throw new RuntimeException("结果解析失败: " + e.getMessage())
        }
    } else {
        String resson = response == null ? "response is null" : response.getStatus() + ""
        throw new RuntimeException("网络请求失败: " + resson)
    }
}

static void main(String[] args) {
    def task = new Task()
//    task.setTaskUrl("https://page.auctions.yahoo.co.jp/jp/auction/o1156395964")
    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/a5d0a5a4a5.html?page=3#CentSrchFilter1")
    println fetch(task)
}