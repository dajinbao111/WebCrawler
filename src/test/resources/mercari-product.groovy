import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task
import com.github.dajinbao.crawler.ImgDownloader

static def fetch(Task task) {
    def DPOP = "eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7ImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoiZm8wSHFXbW55WUwxcy1lMEdEZ3JVc2h1Ri1hcWtOU1FaV0tUYk1WYjJMMCIsInkiOiJyZG1TSU5xOFU1TlY1WFV1TVFsbmVyRGVVUi0xN3BwOWU0TzhxcTA2a0ZvIn19.eyJpYXQiOjE3MjgzODg2ODMsImp0aSI6IjNhNmY2NTA0LWJmOTQtNDY2Yy1iMTQ5LWM2YzE1MzNiNTc0MyIsImh0dSI6Imh0dHBzOi8vYXBpLm1lcmNhcmkuanAvaXRlbXMvZ2V0IiwiaHRtIjoiR0VUIiwidXVpZCI6IjRlNjBjZWJhLWJhOTQtNDg0YS04OWE0LTc4YTc5MDdiOTgwZCJ9.W3v6w0XMSCMng-oQtpiVdS1GWiQ-uzmReea9fI1Du-oBlZzaJcfINbcBZID58JwvaniRBM0pUJymKj6IlkGUQg"
    def IMG_PATH = "D:/Workspaces/go-crawler/uploads/productImg/mercari/"
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    // https://jp.mercari.com/item/m66274034354
    def productId = StrUtil.subAfter(task.getTaskUrl(), "/", true)
    def result = [:]
    HttpResponse response = null;
    try {
        response = HttpRequest.get("https://api.mercari.jp/items/get?id=" + productId + "&include_item_attributes=true&include_product_page_component=true&include_non_ui_item_attributes=true&include_donation=true&include_offer_like_coupon_display=true&include_offer_coupon_display=true")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .header("Dnt", "1")
                .header("Dpop", DPOP)
                .header("origin", "https://jp.mercari.com")
                .header("priority", "u=1, i")
                .header("referer", "https://jp.mercari.com/")
                .header("x-platform", "web")
                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
                .executeAsync()
    } catch (Exception e) {
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def content = response.body()
        try {
            def jsonObj = JSON.parseObject(content)
            def item = jsonObj.getJSONObject("data")
            def product = [:]
            product["productSite"] = "mercari"
            product["productCode"] = item.getString("id")
            product["productTitle"] = item.getString("name")
            product["productPrice"] = item.getString("price")
            product["productUrl"] = "https://jp.mercari.com/item/" + product["productCode"]
            def img = []
            item.getJSONArray("photos").forEach { t ->
                {
                    def imgUrl = StrUtil.subBefore(t as String, "?", true)
                    img << imgUrl
                    ImgDownloader.download(imgUrl, IMG_PATH + product["productCode"] + "/")
                }
            }
            product["productMainImg"] = img[0]
            product["productThumbnail"] = img
            product["productDesc"] = item.getString("description")

            def categories = []
            item.getJSONArray("parent_categories_ntiers").forEach { t ->
                {
                    def ntier = t as JSONObject
                    def category = [:]
                    category["categoryName"] = ntier.getString("name")
                    category["categoryId"] = ntier.getString("id")
                    category["categoryUrl"] = "https://jp.mercari.com/search?category_id=" + category["categoryId"]
                    categories << category
                }
            }
            product["category"] = categories

            def seller = [:]
            seller["sellerName"] = item.getJSONObject("seller").getString("name")
            seller["sellerId"] = item.getJSONObject("seller").getString("id")
            seller["sellerUrl"] = "https://jp.mercari.com/user/profile/" + seller["sellerId"]
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
    task.setTaskUrl("https://jp.mercari.com/item/m66274034354")
    println fetch(task)
}