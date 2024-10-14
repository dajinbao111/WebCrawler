package com.github.dajinbao.crawler;

import com.github.dajinbao.apiserver.api.TaskStatusEnum;
import com.github.dajinbao.apiserver.entity.Task;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class WebDriverCrawler {
    private final GroovyClassLoader groovyClassLoader;

    public WebDriverCrawler() {
        this.groovyClassLoader = new GroovyClassLoader();
    }

    public String fetch(Task task, Map<String, String> scriptMap) {
        log.info("fetch task: {}", task.getTaskUrl());
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
    }
}
