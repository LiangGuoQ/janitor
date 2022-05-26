package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * ClassName EventPushResult
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPushResult implements Serializable {
    /**
     * 已推送的客户端列表
     */
    private Map<String, Object> pushList;
    /**
     * 推送结果标志
     */
    private boolean pushResult;
    /**
     * 错误信息
     */
    private String errMsg;

    public static EventPushResult error(String errMsg) {
        EventPushResult eventPushResult = new EventPushResult();
        eventPushResult.setErrMsg(errMsg);
        eventPushResult.setPushResult(false);
        return eventPushResult;
    }

    public static EventPushResult success(Map<String, Object> pushList) {
        EventPushResult eventPushResult = new EventPushResult();
        eventPushResult.setPushList(pushList);
        eventPushResult.setPushResult(true);
        return eventPushResult;
    }

    @Override
    public String toString() {
        return "EventPushResult{pushList=" + this.pushList + ", pushResult=" + this.pushResult + ", errMsg='" + this.errMsg + '\'' + '}';
    }
}

