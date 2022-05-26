package com.janitor.common.json;

import java.util.List;
import java.util.Map;

/**
 * ClassName AbstractJson
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:28
 */
public abstract class AbstractJson {
    private static JsonFactory defaultJsonFactory = new JacksonFactory();
    private static String defaultDatePattern = "yyyy-MM-dd HH:mm:ss";
    protected String datePattern = null;

    public AbstractJson() {
    }

    static void setDefaultJsonFactory(JsonFactory defaultJsonFactory) {
        if (defaultJsonFactory == null) {
            throw new IllegalArgumentException("defaultJsonFactory can not be null.");
        } else {
            AbstractJson.defaultJsonFactory = defaultJsonFactory;
        }
    }

    static void setDefaultDatePattern(String defaultDatePattern) {
        if (defaultDatePattern != null && !defaultDatePattern.isEmpty()) {
            AbstractJson.defaultDatePattern = defaultDatePattern;
        } else {
            throw new IllegalArgumentException("defaultDatePattern can not be blank.");
        }
    }

    public AbstractJson setDatePattern(String datePattern) {
        if (datePattern != null && !datePattern.isEmpty()) {
            this.datePattern = datePattern;
            return this;
        } else {
            throw new IllegalArgumentException("datePattern can not be blank.");
        }
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public String getDefaultDatePattern() {
        return defaultDatePattern;
    }

    public static AbstractJson getJson() {
        return defaultJsonFactory.getJson();
    }

    public abstract String toJson(Object obj);

    public abstract <T> T parse(String json, Class<T> clz);

    public abstract <T> T parse(byte[] jsonBytes, Class<T> clz);

    public abstract <T> List<T> parseArray(String json, Class<T> clz);

    public abstract <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass);

    public abstract <K, V> Map<K, V> parseMap(byte[] jsonBytes, Class<K> kClass, Class<V> vClass);

    public abstract <K, V> List<Map<K, V>> parseMapArray(String json, Class<K> kClass, Class<V> vClass);
}
