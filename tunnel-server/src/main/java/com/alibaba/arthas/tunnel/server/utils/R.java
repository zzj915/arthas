package com.alibaba.arthas.tunnel.server.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Optional;

/**
 * REST API 返回结果
 *
 * @author hubin
 * @since 2018-06-05
 */
@Data
@Accessors(chain = true)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 3490211779130241508L;

    /**
     * 业务错误码
     */
    private long code;
    /**
     * 结果集
     */
    private T data;
    /**
     * 描述
     */
    private String msg;

    public R() {
        // to do nothing
    }

    public R(ApiErrorCode errorCode) {
        errorCode = Optional.ofNullable(errorCode).orElse(ApiErrorCode.FAILED);
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ApiErrorCode.SUCCESS.code, ApiErrorCode.SUCCESS.msg);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, ApiErrorCode.SUCCESS.code, msg);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, ApiErrorCode.FAILED.getCode(), msg);
    }

    public static <T> R<T> failed(long code, String msg) {
        Assert.isTrue(code != 0, "code不允许为0");
        return restResult(null, code, msg);
    }

    public static <T> R<T> restResult(T data, long code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public boolean ok() {
        return ApiErrorCode.SUCCESS.getCode() == code;
    }

    @Override
    public String toString() {
        return String.format("{\"code\":\"%s\", \"msg\":\"%s\"} ", code, msg);
    }

    /**
     * 服务间调用非业务正常，异常直接释放
     */
    public T serviceData() throws Exception {
        if (!ok()) {
            throw new Exception(this.msg);
        }
        return data;
    }


    public enum ApiErrorCode {
        /**
         * 失败
         */
        FAILED(-1, "操作失败"),
        /**
         * 成功
         */
        SUCCESS(0, "执行成功");

        private final long code;
        private final String msg;

        ApiErrorCode(final long code, final String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static ApiErrorCode fromCode(long code) {
            ApiErrorCode[] ecs = ApiErrorCode.values();
            for (ApiErrorCode ec : ecs) {
                if (ec.getCode() == code) {
                    return ec;
                }
            }
            return SUCCESS;
        }

        public long getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return String.format(" ErrorCode:{code=%s, msg=%s} ", code, msg);
        }
    }

}
