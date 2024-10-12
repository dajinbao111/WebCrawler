package com.github.dajinbao.apiserver.service;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.HashUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.dajinbao.apiserver.api.TaskStatusEnum;
import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.entity.Task;
import com.github.dajinbao.crawler.HttpCrawler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class TaskService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final MongoTemplate template;

    public TaskService(MongoTemplate template) {
        this.template = template;
    }

    public void create(String taskType, String taskUrl) {
        Task task = new Task();
        task.setTaskType(taskType);
        task.setTaskUrl(taskUrl);
        task.setTaskStatus(TaskStatusEnum.PENDING.getCode());
        task.setCreateAt(LocalDateTime.now());
        template.insert(task);
    }

    public List<Task> batchGet(Integer limit) {
        List<Task> taskList = template.find(query(where("taskStatus").is(TaskStatusEnum.PENDING.getCode())).limit(limit), Task.class);
        List<String> ids = taskList.stream().map(Task::getId).toList();
        template.updateMulti(query(where("id").in(ids)), new Update().set("taskStatus", TaskStatusEnum.RUNNING.getCode()), Task.class);
        return taskList;
    }

    @Scheduled(fixedRate = 1000 * 30)
    public void fetchTask() {
        List<Task> taskList = batchGet(10);
        if (!taskList.isEmpty()) {
            Map<String, String> scriptMap = loadScriptFile();
            HttpCrawler httpCrawler = new HttpCrawler();
            List<String> result = httpCrawler.fetch(taskList, scriptMap);
            // 更新任务状态
            for (Task task : taskList) {
                task.setUpdatedAt(LocalDateTime.now());
                template.updateFirst(query(where("id").is(task.getId())),
                        new Update().set("taskStatus", task.getTaskStatus()).set("failReason", task.getFailReason()).set("updatedAt", LocalDateTime.now()), Task.class);
            }

            for (String res : result) {
                JSONObject jsonObject = JSON.parseObject(res);
                if (jsonObject.containsKey("tasks")) {
                    jsonObject.getJSONArray("tasks").forEach(item -> {
                        JSONObject taskObj = (JSONObject) item;
                        // 创建任务
                        create(taskObj.getString("taskType"), taskObj.getString("taskUrl"));
                    });
                } else if (jsonObject.containsKey("products")) {
                    JSONArray productArr = jsonObject.getJSONArray("products");
                    for (int i = 0; i < productArr.size(); i++) {
                        JSONObject productObj = productArr.getJSONObject(i);
                        String productUrl = productObj.getString("productUrl");
                        productObj.put("_id", Long.toHexString(HashUtil.mixHash(productUrl)));
                        productObj.put("updatedAt", LocalDateTime.now());
                        template.save(productObj, "product");
                    }
                } else if (jsonObject.containsKey("product")) {
                    JSONObject productObj = jsonObject.getJSONObject("product");
                    String productUrl = productObj.getString("productUrl");
                    productObj.put("_id", Long.toHexString(HashUtil.mixHash(productUrl)));
                    productObj.put("updatedAt", LocalDateTime.now());
                    template.save(productObj, "product");
                }
            }
        }
    }

    public void upsert(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir + "script/");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public Map<String, String> loadScriptFile() {
        Map<String, String> scriptMap = new HashMap<>();
        Path uploadPath = Paths.get(uploadDir + "script/");

        if (Files.exists(uploadPath)) {
            try {
                Files.list(uploadPath).forEach(file -> {
                    try {
                        scriptMap.put(FileNameUtil.getPrefix(file.getFileName().toString()), new String(Files.readAllBytes(file)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return scriptMap;
    }

    public Map<String, Object> status() {
        int pendingCount = template.find(query(where("createAt").lt(LocalDateTime.now().minusDays(1)).and("taskStatus").is(TaskStatusEnum.PENDING.getCode())), Task.class).size();
        int runningCount = template.find(query(where("createAt").lt(LocalDateTime.now().minusDays(1)).and("taskStatus").is(TaskStatusEnum.RUNNING.getCode())), Task.class).size();
        int failedCount = template.find(query(where("createAt").lt(LocalDateTime.now().minusDays(1)).and("taskStatus").is(TaskStatusEnum.FAILED.getCode())), Task.class).size();
        int finishedCount = template.find(query(where("createAt").lt(LocalDateTime.now().minusDays(1)).and("taskStatus").is(TaskStatusEnum.FINISHED.getCode())), Task.class).size();
        Map<String, Object> resultMap = new HashMap<>(4);
        resultMap.put(TaskStatusEnum.PENDING.getCode(), pendingCount);
        resultMap.put(TaskStatusEnum.RUNNING.getCode(), runningCount);
        resultMap.put(TaskStatusEnum.FAILED.getCode(), failedCount);
        resultMap.put(TaskStatusEnum.FINISHED.getCode(), finishedCount);
        return resultMap;
    }

    public Page<Map> list(String taskStatus, int pageNo, int pageSize) {
        long totalCount = template.count(query(where("taskStatus").is(taskStatus)), "task");
        Page<Map> page = Page.of(pageNo, pageSize, totalCount);
        List<Map> taskList = template.find(query(where("taskStatus").is(taskStatus)).skip((long) (pageNo - 1) * pageSize).limit(pageSize), Map.class, "task");
        page.setRecords(taskList);
        return page;
    }
}
