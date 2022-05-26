package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName EventResultNotifyReq
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResultNotifyReq implements Serializable {
    /**
     * key
     */
    private String key;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 事件内容
     */
    private String eventValue;
    /**
     * 响应
     */
    private Object response;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class EventError {
        private String listenerName;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class ResponseContent {
        private Integer code;
        private List<EventError> errors;
    }
}
