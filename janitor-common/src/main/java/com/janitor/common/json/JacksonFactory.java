package com.janitor.common.json;

/**
 * ClassName JacksonFactory
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:29
 */
public class JacksonFactory implements JsonFactory {
    private static final JacksonFactory FACTORY = new JacksonFactory();

    public JacksonFactory() {
    }

    public static JacksonFactory inst() {
        return FACTORY;
    }

    @Override
    public AbstractJson getJson() {
        return new Jackson();
    }
}
