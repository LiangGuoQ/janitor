package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ClassName EventErrorEntity
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventErrorEntity implements Serializable {
    /**
     * 侦听器名称
     */
    private String listenerName;
    /**
     * 错误消息
     */
    private String errorMessage;
}
