package com.github.dajinbao.apiserver.common.model;

/**
 * REST返回码
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    CLIENT_ERROR(400, "客户端认证失败"),
    UNAUTHORIZED(401, "未登录或者登录超时"),
    INVALID_TOKEN(401, "不正确的token或已过期"),
    ACCOUNT_ERROR(402, "用户名或密码错误"),
    APP_ERROR(402, "应用不存在"),
    FORBIDDEN(403, "没有相关权限"),
    VALIDATE_FAILED(404, "参数检验失败"),
    NOT_FOUND(404, "找不到该服务");


    private final int code;

    private final String message;

    private ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
