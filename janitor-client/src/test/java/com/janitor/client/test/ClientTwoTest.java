package com.janitor.client.test;

import com.janitor.client.listener.AbstractEventListener;
import com.janitor.client.JanitorHelper;
import com.janitor.client.processor.ConfigProcessor;
import com.janitor.common.enums.EventTypeEnums;
import lombok.SneakyThrows;

import java.util.Properties;

/**
 * ClassName ClientTwoTest
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 14:36
 */
public class ClientTwoTest {

    @SneakyThrows
    public static void main(String[] args) {
        JanitorHelper janitorHelper = new JanitorHelper("/app/client", "client2", "client2");
        janitorHelper.addEventListener(
                new AbstractEventListener(3, 10, false) {
                    @Override
                    public void exec(String eventValue) {
                        System.out.println("client2 收到消息：" + eventValue);
                    }

                    @Override
                    public EventTypeEnums event() {
                        return EventTypeEnums.CACHE_EXPIRE;
                    }
                })
                .addProcessor(new ConfigProcessor() {
                    @Override
                    public void onUpdate(Properties properties) {
                        for (Object key : properties.keySet()) {
                            System.out.println("client1 收到配置更新： key：" + key + ", value：" + properties.get(key));
                        }
                    }
                })
                .setJanitorServerHostPort("6237")
                .setJanitorServerIp("127.0.0.1")
                .start();
        Thread.sleep(1200000L);
    }
}
