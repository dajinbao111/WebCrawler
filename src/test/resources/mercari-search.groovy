import cn.hutool.core.util.ReUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson2.JSON
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task

static def fetch(Task task) {
    def DPOP = "eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7ImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoiZm8wSHFXbW55WUwxcy1lMEdEZ3JVc2h1Ri1hcWtOU1FaV0tUYk1WYjJMMCIsInkiOiJyZG1TSU5xOFU1TlY1WFV1TVFsbmVyRGVVUi0xN3BwOWU0TzhxcTA2a0ZvIn19.eyJpYXQiOjE3MjgzNzc3MjAsImp0aSI6IjMwYjljMDU5LWU2YjEtNDBmNy1iNTQzLWJmZjcyYjRhY2JiNyIsImh0dSI6Imh0dHBzOi8vYXBpLm1lcmNhcmkuanAvdjIvZW50aXRpZXM6c2VhcmNoIiwiaHRtIjoiUE9TVCIsInV1aWQiOiI0ZTYwY2ViYS1iYTk0LTQ4NGEtODlhNC03OGE3OTA3Yjk4MGQifQ.GY0795p_7lfLqmbt4mYMN-vEh6WGNtKWGFZyYyxnea4jXRWyl4RCnUovovSgchZwb7_uwyhn3Xh61C-qUwnUQQ"
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    // https://jp.mercari.com/search?category_id=2635
    def categoryId = ReUtil.get("category_id=(\\d+)", task.getTaskUrl(), 1) as Integer
    def result = [:]
    HttpResponse response = null;
    try {
        response = HttpRequest.post("https://api.mercari.jp/v2/entities:search")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .header("Dnt", "1")
                .header("Dpop", DPOP)
                .header("origin", "https://jp.mercari.com")
                .header("priority", "u=1, i")
                .header("referer", "https://jp.mercari.com/")
                .header("x-country-code", "JP")
                .header("x-platform", "web")
                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
                .body("{\"userId\":\"\",\"pageSize\":120,\"pageToken\":\"\",\"searchSessionId\":\"42d72376d6671d9bb3ec1c6d8bb1176e\",\"indexRouting\":\"INDEX_ROUTING_UNSPECIFIED\",\"thumbnailTypes\":[],\"searchCondition\":{\"keyword\":\"\",\"excludeKeyword\":\"\",\"sort\":\"SORT_SCORE\",\"order\":\"ORDER_DESC\",\"status\":[],\"sizeId\":[],\"categoryId\":[" + categoryId + "],\"brandId\":[],\"sellerId\":[],\"priceMin\":0,\"priceMax\":0,\"itemConditionId\":[],\"shippingPayerId\":[],\"shippingFromArea\":[],\"shippingMethod\":[],\"colorId\":[],\"hasCoupon\":false,\"attributes\":[],\"itemTypes\":[],\"skuIds\":[],\"shopIds\":[]},\"defaultDatasets\":[\"DATASET_TYPE_MERCARI\",\"DATASET_TYPE_BEYOND\"],\"serviceFrom\":\"suruga\",\"withItemBrand\":true,\"withItemSize\":false,\"withItemPromotions\":true,\"withItemSizes\":true,\"withShopname\":false,\"useDynamicAttribute\":true,\"withSuggestedItems\":true,\"withOfferPricePromotion\":true,\"withProductSuggest\":true,\"withProductArticles\":false,\"withSearchConditionId\":false}")
                .executeAsync()
    } catch (Exception e) {
        task.setTaskStatus(TaskStatusEnum.FAILED.getCode())
        task.setFailReason("网络请求失败: " + task.getTaskType())
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def content = response.body()
        def jsonObj = JSON.parseObject(content)
        def items = jsonObj.getJSONArray("items")
        def products = []
        def tasks = []
        for (i in 0..<items.size()) {
            def item = items.getJSONObject(i)
            def product = [:]
            def newTask = [:]
            product["productCode"] = item.getString("id")
            product["productUrl"] = "https://jp.mercari.com/item/" + product["productCode"]
            product["productTitle"] = item.getString("name")
            product["productPrice"] = item.getString("price")
            def img = []
            item.getJSONArray("thumbnails").forEach { t ->
                img << t
            }
            product["productImgUrl"] = img
            def seller = [:]
            seller["sellerId"] = item.getString("sellerId")
            seller["sellerUrl"] = "https://jp.mercari.com/user/profile/" + seller["sellerId"]
            product["seller"] = seller
            products << product

            newTask["taskType"] = "mercari-product";
            newTask["taskUrl"] = product["productUrl"]
            tasks << newTask
        }
        result["tasks"] = tasks
        result["products"] = products
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
    task.setTaskUrl("https://jp.mercari.com/search?category_id=2635")
    println fetch(task)
}