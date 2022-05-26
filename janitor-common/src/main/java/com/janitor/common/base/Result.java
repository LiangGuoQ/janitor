package com.janitor.common.base;

import lombok.Builder;
import lombok.Data;

/**
 * ClassName Result
 * Description
 *
 * @author lianggq4
 * Date 2022/5/24 16:24
 */
@Data
@Builder
public class Result {

    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回消息
     */
    private String msg;
    /**
     * 返回数据
     */
    private Object data;

    public static Result success() {
        return Result.builder()
                .code(0)
                .build();
    }

    public static Result success(Object data) {
        return Result.builder()
                .code(0)
                .data(data)
                .build();
    }

    public static Result success(Object data, String msg) {
        return Result.builder()
                .code(0)
                .data(data)
                .msg(msg)
                .build();
    }

    public static Result fail(String msg) {
        return Result.builder()
                .code(9999)
                .msg(msg)
                .build();
    }
}
