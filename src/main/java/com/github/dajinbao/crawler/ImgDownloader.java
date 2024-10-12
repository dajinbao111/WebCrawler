package com.github.dajinbao.crawler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImgDownloader {

    public static void download(String url, String filePath) {
        String fileName = StrUtil.subAfter(url, "/", true);

        Path uploadPath = Paths.get(filePath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path file = Paths.get(filePath + "/" + fileName);
        if (!Files.exists(file)) {
            final HttpResponse response = HttpRequest.get(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                    .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890)))
                    .setFollowRedirects(true)
                    .executeAsync();

            if (response.isOk()) {
                response.writeBody(filePath + "/" + fileName);
            }
        }
    }
}
