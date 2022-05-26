package com.janitor.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName Jackson
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:30
 */
public class Jackson extends AbstractJson {
    private static boolean defaultGenerateNullValue = true;
    protected Boolean generateNullValue = null;
    protected ObjectMapper objectMapper = new ObjectMapper();

    public Jackson() {
        this.config();
    }

    protected void config() {
        this.objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        this.objectMapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public static void setDefaultGenerateNullValue(boolean defaultGenerateNullValue) {
        Jackson.defaultGenerateNullValue = defaultGenerateNullValue;
    }

    public Jackson setGenerateNullValue(boolean generateNullValue) {
        this.generateNullValue = generateNullValue;
        return this;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public static Jackson getJson() {
        return new Jackson();
    }

    @Override
    public String toJson(Object object) {
        try {
            String dp = this.datePattern != null ? this.datePattern : this.getDefaultDatePattern();
            if (dp != null) {
                this.objectMapper.setDateFormat(new SimpleDateFormat(dp));
            }

            boolean pnv = this.generateNullValue != null ? this.generateNullValue : defaultGenerateNullValue;
            if (!pnv) {
                this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }

            return this.objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @Override
    public <T> T parse(String jsonString, Class<T> type) {
        try {
            return this.objectMapper.readValue(jsonString, type);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @Override
    public <T> T parse(byte[] jsonString, Class<T> type) {
        try {
            return this.objectMapper.readValue(jsonString, type);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> parseArray(String jsonString, Class<T> type) {
        try {
            JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(List.class, type);
            return this.objectMapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @Override
    public <K, V> Map<K, V> parseMap(String jsonString, Class<K> keyType, Class<V> valueType) {
        try {
            JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(Map.class, keyType, valueType);
            return this.objectMapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @Override
    public <K, V> Map<K, V> parseMap(byte[] jsonByte, Class<K> keyType, Class<V> valueType) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(Map.class, keyType, valueType);

        try {
            return this.objectMapper.readValue(jsonByte, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <K, V> List<Map<K, V>> parseMapArray(String jsonStr, Class<K> keyType, Class<V> valType) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(List.class, Map.class);

        try {
            List<?> list = this.objectMapper.readValue(jsonStr, javaType);
            List<Map<K, V>> values = new ArrayList<>();
            list.forEach((row) -> values.add(this.parseMap(this.toJson(row), keyType, valType)));
            return values;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
