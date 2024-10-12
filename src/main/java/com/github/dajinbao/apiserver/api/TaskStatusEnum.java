package com.github.dajinbao.apiserver.api;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    PENDING("0", "未开始"),
    RUNNING("1", "进行中"),
    DONE("2", "已完成"),
    FAILED("3", "失败"),
    FINISHED("4", "已结束");

    private final String code;
    private final String description;

    TaskStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
