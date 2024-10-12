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
        task.setTaskStatus(TaskStatusEnum.FAILED.getCode())
        task.setFailReason("网络请求失败: " + task.getTaskType())
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def doc = Jsoup.parse(response.body())

        def tasks = []
        doc.select("ul.Products__items > li.Product").each {
            def url = it.select("p.Product__title > a[href]").get(0).attr("href")
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

        result["tasks"] = tasks
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
    task.setTaskUrl("https://auctions.yahoo.co.jp/seller/oxztx00272")
    println fetch(task)
}