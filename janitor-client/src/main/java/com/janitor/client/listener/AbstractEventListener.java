package com.janitor.client.listener;

import com.janitor.common.enums.EventTypeEnums;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName AbstractEventListener
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:13
 */
@Data
@AllArgsConstructor
public abstract class AbstractEventListener {
    /**
     * 重试次数
     */
    private int retries;
    /**
     * 重试时间间隔
     */
    private int retryInterval;
    /**
     * 是否区间重试
     * 公式：listener.getRetryInterval() * Math.pow(2.0D, retryTime)
     */
    private boolean multipleInterval;

    /**
     * 根据事件类型，处理
     *
     * @param eventValue 事件消息
     * @throws Exception 异常
     */
    public abstract void exec(String eventValue) throws Exception;

    /**
     * 事件类型
     *
     * @return EventTypeEnums
     */
    public abstract EventTypeEnums event();

}
