import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.github.dajinbao.apiserver.entity.Task

static def fetch(Task task) {
    def IMG_PATH = "c:/webcrawler/uploads/productImg/mercari/"
    def PROXY_HOST = "127.0.0.1"
    def PROXY_PROT = 7890

    //https://static.mercdn.net/item/detail/orig/photos/m52869268432_1.jpg
    //https://assets.mercari-shops-static.com/-/large/plain/m7QTkPw9SMJu6YP43SBrQe.jpg

    HttpResponse response = null;
    try {
        response = HttpRequest.get(task.getTaskUrl())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .header("Dnt", "1")
                .header("referer", "https://jp.mercari.com/")
                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PROT)))
                .setFollowRedirects(true)
                .executeAsync()
    } catch (Exception e) {
        throw new RuntimeException("网络请求失败: " + e.getMessage())
    }

    if (response != null && response.isOk()) {
        def fileName = task.getTaskUrl().split("/").last()
        def productCode = null
        if (StrUtil.contains(fileName, "_")) {
            productCode = StrUtil.subBefore(fileName, "_", true)
        }
        if (StrUtil.contains(fileName, ".")) {
            productCode = StrUtil.subBefore(fileName, ".", true)
        }

        response.writeBody(IMG_PATH + productCode + "/" + fileName)
        return null
    } else {
        String resson = response == null ? "response is null" : response.getStatus() + ""
        throw new RuntimeException("网络请求失败: " + resson)
    }
}

static void main(String[] args) {
    def task = new Task()
    task.setTaskUrl("https://static.mercdn.net/item/detail/orig/photos/m52869268432_1.jpg")
    println fetch(task)
}