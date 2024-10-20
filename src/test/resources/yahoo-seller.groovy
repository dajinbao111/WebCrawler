import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson2.JSON
import com.github.dajinbao.apiserver.api.TaskStatusEnum
import com.github.dajinbao.apiserver.entity.Task
import org.jsoup.Jsoup

static def fetch(Task task) {
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
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        try {
            def doc = Jsoup.parse(response.body())

            def tasks = []
            doc.select("ul.Products__items > li.Product").each {
                def url = it.select("p.Product__title > a[href]").get(0).attr("href")
                def newTask = [:]
                newTask["taskType"] = "yahoo-product"
                newTask["taskUrl"] = url
                tasks << newTask
            }
            doc.select("ul.elItems li.elItem div.elName a").each {
                def url = it.attr("href")
                def newTask = [:]
                newTask["taskType"] = "yahoo-product"
                newTask["taskUrl"] = url
                tasks << newTask
            }

            def next = doc.select("ul.Pager__lists > li.Pager__list--next > a[href]")
            if (next.size() > 0) {
                def newTask = [:]
                newTask["taskType"] = "yahoo-seller";
                newTask["taskUrl"] = next.get(0).attr("href")
                tasks << newTask
            }
            next = doc.select("div.elItem li.elNext a[href]")
            if (next.size() > 0) {
                def newTask = [:]
                newTask["taskType"] = "yahoo-seller";
                newTask["taskUrl"] = next.get(0).attr("href")
                tasks << newTask
            }

            result["tasks"] = tasks
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
//    task.setTaskUrl("https://auctions.yahoo.co.jp/seller/oxztx00272")
//    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/a5d0a5a4a5.html")
//    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/a5d0a5a4a5.html?page=49#CentSrchFilter1")
//    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/search.html")
//    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/search.html?page=1#CentSrchFilter1")
    task.setTaskUrl("https://store.shopping.yahoo.co.jp/pstokyo/a5d0a5a4a5.html?page=3#CentSrchFilter1")
    println fetch(task)
}