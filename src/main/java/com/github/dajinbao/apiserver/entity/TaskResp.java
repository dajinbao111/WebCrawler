package com.github.dajinbao.apiserver.entity;

import lombok.Data;

@Data
public class TaskResp {

    private String id;
    private String taskType;
    private String taskUrl;
    private String taskStatus;
    private String failReason;
    private String createAt;
    private String updatedAt;

}
