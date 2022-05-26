package com.janitor.client.processor;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ClassName ConfigProcessor
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:15
 */
public interface ConfigProcessor {
    /**
     * 配置生命周期：初始化
     *
     * @param properties 配置信息
     */
    default void onLoad(Properties properties) {
    }

    /**
     * 配置生命周期：配置创建
     *
     * @param properties 配置信息
     */
    default void onAdd(Properties properties) {
    }

    /**
     * 配置生命周期：配置更新
     *
     * @param properties 配置信息
     */
    default void onUpdate(Properties properties) {
    }

    /**
     * 配置生命周期：配置删除
     *
     * @param properties 配置信息
     */
    default void onDelete(Properties properties) {
    }

    /**
     * 匹配
     *
     * @param properties 配置信息
     * @return 配置信息
     */
    default Properties match(Properties properties) {
        return properties;
    }

    /**
     * 匹配符合前缀的key
     *
     * @param source    配置信息
     * @param keyPrefix key的前缀
     * @return 配置信息
     */
    default Properties defaultMatch(Properties source, String keyPrefix) {
        Map<String, String> map = source.entrySet().stream()
                .filter((entry) -> entry.getKey().toString().startsWith(keyPrefix))
                .collect(Collectors.toMap((e) -> e.getKey().toString(), (e) -> e.getValue().toString()));
        Properties prop = new Properties();
        prop.putAll(map);
        return prop;
    }
}
