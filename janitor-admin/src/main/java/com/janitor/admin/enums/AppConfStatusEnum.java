package com.janitor.admin.enums;

/**
 * ClassName AppConfStatusEnum
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 10:00
 */
public enum AppConfStatusEnum {
    /**
     * 0-已发布 1-新增 2-修改 3-删除 4-发布后修改 5-发布后删除
     */
    PUBLISHED(0, "已发布"),
    CREATE(1, "新增"),
    MODIFY(2, "修改"),
    DELETE(3, "删除"),
    PUBLISHED_MODIFY(4, "发布后修改"),
    PUBLISHED_DELETE(5, "发布后删除");

    private final Integer code;

    private final String des;

    AppConfStatusEnum(Integer code, String des) {
        this.code = code;
        this.des = des;
    }

    public Integer getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    public static AppConfStatusEnum getFromCode(Integer status) {
        AppConfStatusEnum[] enums = values();
        int len = enums.length;

        for (AppConfStatusEnum anEnum : enums) {
            if (anEnum.getCode().equals(status)) {
                return anEnum;
            }
        }

        return PUBLISHED;
    }
}
