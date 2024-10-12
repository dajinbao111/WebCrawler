package com.github.dajinbao.crawler;

import com.github.dajinbao.apiserver.api.TaskStatusEnum;
import com.github.dajinbao.apiserver.entity.Task;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class HttpCrawler {

    private final ExecutorService executorService;
    private final GroovyClassLoader groovyClassLoader;

    public HttpCrawler(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.groovyClassLoader = new GroovyClassLoader();
    }

    public HttpCrawler() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.groovyClassLoader = new GroovyClassLoader();
    }

    public List<String> fetch(List<Task> taskList, Map<String, String> scriptMap) {
        List<Future<String>> futureList = new ArrayList<>();

        // 提交所有任务到线程池
        for (Task task : taskList) {
            final String taskUrl = task.getTaskUrl();
            Future<String> future = executorService.submit(() -> {
                log.info("fetch task: {}", taskUrl);
                if (scriptMap.containsKey(task.getTaskType())) {
                    try {
                        String script = scriptMap.get(task.getTaskType());
                        Class<?> groovyClass = groovyClassLoader.parseClass(script);
                        GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
                        Object object = groovyObject.invokeMethod("fetch", new Object[]{task});
                        return object.toString();
                    } catch (Exception e) {
                        log.error("{}", e.getMessage());
                        task.setTaskStatus(TaskStatusEnum.FAILED.getCode());
                        task.setFailReason(e.getMessage());
                    }
                } else {
                    task.setTaskStatus(TaskStatusEnum.FAILED.getCode());
                    task.setFailReason("缺失解析脚本: " + task.getTaskType());
                }
                return null;
            });
            futureList.add(future);
        }

        List<String> result = futureList.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                log.error("任务执行失败", e);
            }
            return null;
        }).toList();

        executorService.shutdown();
        return result;
    }

}
