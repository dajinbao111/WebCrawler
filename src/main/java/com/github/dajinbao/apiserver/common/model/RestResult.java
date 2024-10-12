package com.github.dajinbao.apiserver.common.model;

import java.io.Serializable;

/**
 * REST返回结果对象
 */
public class RestResult<T> implements Serializable {
    private static final long serialVersionUID = -1219332960938983860L;

    private int code;

    private String message;

    private T data;

    public RestResult() {
    }

    public RestResult(int code, String message, T data) {
        this.code = code;
        this.setMessage(message);
        this.data = data;
    }

    public RestResult(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public RestResult(int code, String message) {
        this.code = code;
        this.setMessage(message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RestResult{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
    }

    public static <T> RestResult<T> success() {
        return RestResult.<T>builder().withCode(ResultCode.SUCCESS.getCode()).withMsg(ResultCode.SUCCESS.getMessage()).build();
    }

    public static <T> RestResult<T> success(T data) {
        return RestResult.<T>builder().withCode(ResultCode.SUCCESS.getCode()).withData(data).build();
    }

    public static <T> RestResult<T> success(String msg, T data) {
        return RestResult.<T>builder().withCode(ResultCode.SUCCESS.getCode()).withMsg(msg).withData(data).build();
    }

    public static <T> RestResult<T> success(int code, T data) {
        return RestResult.<T>builder().withCode(code).withData(data).build();
    }

    public static <T> RestResult<T> failed() {
        return RestResult.<T>builder().withCode(ResultCode.FAILED.getCode()).withMsg(ResultCode.FAILED.getMessage()).build();
    }

    public static <T> RestResult<T> failed(String errMsg) {
        return RestResult.<T>builder().withCode(ResultCode.FAILED.getCode()).withMsg(errMsg).build();
    }

    public static <T> RestResult<T> failed(int code, T data) {
        return RestResult.<T>builder().withCode(code).withData(data).build();
    }

    public static <T> RestResult<T> failed(int code, T data, String errMsg) {
        return RestResult.<T>builder().withCode(code).withData(data).withMsg(errMsg).build();
    }

    public static <T> RestResult<T> failedWithMsg(int code, String errMsg) {
        return RestResult.<T>builder().withCode(code).withMsg(errMsg).build();
    }

    public static <T> ResResultBuilder<T> builder() {
        return new ResResultBuilder<T>();
    }

    public static final class ResResultBuilder<T> {

        private int code;

        private String errMsg;

        private T data;

        private ResResultBuilder() {
        }

        public ResResultBuilder<T> withCode(int code) {
            this.code = code;
            return this;
        }

        public ResResultBuilder<T> withMsg(String errMsg) {
            this.errMsg = errMsg;
            return this;
        }

        public ResResultBuilder<T> withData(T data) {
            this.data = data;
            return this;
        }

        /**
         * Build result.
         *
         * @return result
         */
        public RestResult<T> build() {
            RestResult<T> restResult = new RestResult<T>();
            restResult.setCode(code);
            restResult.setMessage(errMsg);
            restResult.setData(data);
            return restResult;
        }
    }
}
