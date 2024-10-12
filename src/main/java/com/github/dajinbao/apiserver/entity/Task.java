package com.github.dajinbao.apiserver.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {

    private String id;
    private String taskType;
    private String taskUrl;
    private String taskStatus;
    private String failReason;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
