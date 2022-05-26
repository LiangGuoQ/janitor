package com.janitor.common.enums;

/**
 * ClassName EventTypeEnums
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:24
 */
public enum EventTypeEnums {
    /**
     * 事件类型枚举
     */
    GRAY_PUBLISH("灰度发布", 1),
    CACHE_EXPIRE("失效缓存", 0);

    private final String name;
    private final Integer code;

    EventTypeEnums(String name, Integer code) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getCode() {
        return this.code;
    }
}
