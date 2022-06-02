package com.janitor.admin.enums;

/**
 * ClassName AppEventPushResultEnum
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/6/1 13:15
 */
public enum AppEventPushResultEnum {
    /**
     * 推送结果 2-未知 1-成功 0-失败
     */
    FAIL(0, "失败"),
    SUCCESS(1, "成功"),
    UNKNOWN(2, "未知"),
    ;

    private final Integer code;

    private final String des;

    AppEventPushResultEnum(Integer code, String des) {
        this.code = code;
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    public Integer getCode() {
        return code;
    }
}
