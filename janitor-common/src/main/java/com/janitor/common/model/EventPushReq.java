package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName EventPushReq
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 17:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPushReq implements Serializable {
    /**
     * 应用程序名称
     */
    private String appName;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 事件内容
     */
    private String eventValue;
    /**
     * 目标应用程序名字
     */
    private List<String> targetAppNames;

    @Override
    public String toString() {
        return "EventPushReq{appName='" + this.appName + '\'' + ", eventType='" + this.eventType + '\'' + ", eventValue='" + this.eventValue + '\'' + '}';
    }
}
