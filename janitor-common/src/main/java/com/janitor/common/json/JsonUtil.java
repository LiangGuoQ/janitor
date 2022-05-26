package com.janitor.common.json;

import java.util.List;
import java.util.Map;

/**
 * ClassName JsonUtil
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:27
 */
public class JsonUtil {
    public JsonUtil() {
    }

    public static String toJson(Object object) {
        return object instanceof String ? String.valueOf(object) : AbstractJson.getJson().toJson(object);
    }

    public static <T> T parse(String jsonString, Class<T> type) {
        return AbstractJson.getJson().parse(jsonString, type);
    }

    public static <T> T parse(byte[] jsonString, Class<T> type) {
        return AbstractJson.getJson().parse(jsonString, type);
    }

    public static <T> List<T> parseArray(String jsonString, Class<T> type) {
        return AbstractJson.getJson().parseArray(jsonString, type);
    }

    public static <K, V> Map<K, V> parseMap(String jsonString, Class<K> keyType, Class<V> valueType) {
        return AbstractJson.getJson().parseMap(jsonString, keyType, valueType);
    }

    public static <K, V> Map<K, V> parseMap(byte[] jsonByte, Class<K> keyType, Class<V> valueType) {
        return AbstractJson.getJson().parseMap(jsonByte, keyType, valueType);
    }

    public static <K, V> List<Map<K, V>> parseMapArray(String jsonStr, Class<K> keyType, Class<V> valType) {
        return AbstractJson.getJson().parseMapArray(jsonStr, keyType, valType);
    }

    public static boolean isJson(String str) {
        return isJsonObj(str) || isJsonArray(str);
    }

    public static boolean isJsonObj(String str) {
        if (str == null) {
            return false;
        } else {
            str = str.trim();
            if (str.isEmpty()) {
                return false;
            } else {
                return str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}';
            }
        }
    }

    public static boolean isJsonArray(String str) {
        if (str == null) {
            return false;
        } else {
            str = str.trim();
            if (str.isEmpty()) {
                return false;
            } else {
                return str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']';
            }
        }
    }
}
