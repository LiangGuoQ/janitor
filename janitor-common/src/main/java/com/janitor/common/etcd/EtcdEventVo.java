package com.janitor.common.etcd;

import io.etcd.jetcd.watch.WatchEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * ClassName EtcdEventVo
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:21
 */
@Data
@Builder
@AllArgsConstructor
public class EtcdEventVo {
    /**
     * 当前版本
     */
    private EtcdEventKeyValueVo current;
    /**
     * 前一个版本
     */
    private EtcdEventKeyValueVo pre;
    /**
     * etcd操作类型
     */
    private EtcdEventVo.EventType eventType;

    public EtcdEventVo() {
        this.eventType = EtcdEventVo.EventType.UNRECOGNIZED;
    }

    @Override
    public String toString() {
        return "EventType=" + this.eventType.name() + ", pre=" + this.pre.toString() + ", current=" + this.current.toString();
    }

    public static EtcdEventVo valueOf(WatchEvent we) {
        EtcdEventVo event = new EtcdEventVo();
        switch(we.getEventType()) {
            case PUT:
                event.setEventType(EtcdEventVo.EventType.PUT);
                break;
            case DELETE:
                event.setEventType(EtcdEventVo.EventType.DELETE);
                break;
            default:
                break;
        }

        event.setCurrent(EtcdEventKeyValueVo.valueOf(we.getKeyValue()));
        event.setPre(EtcdEventKeyValueVo.valueOf(we.getPrevKV()));
        return event;
    }

    public enum EventType {
        /**
         * 事件类型
         */
        PUT,
        DELETE,
        UNRECOGNIZED;

        EventType() {
        }
    }
}
