package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ClassName PushData
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushData {
    /**
     * 应用名称
     */
    private String app;
    /**
     * 数据
     */
    private Map<String, String> data;
}
