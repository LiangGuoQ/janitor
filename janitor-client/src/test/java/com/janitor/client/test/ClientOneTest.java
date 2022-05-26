package com.janitor.client.test;

import com.janitor.client.listener.AbstractEventListener;
import com.janitor.client.JanitorHelper;
import com.janitor.client.processor.ConfigProcessor;
import com.janitor.common.enums.EventTypeEnums;
import lombok.SneakyThrows;

import java.util.Properties;

/**
 * ClassName ClientOneTest
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 14:36
 */
public class ClientOneTest {

    @SneakyThrows
    public static void main(String[] args) {
        /*
         * JanitorHelper:
         *      localPath：应用路径，存储事件文件、事件文件索引等，宿主机多个客户端应确保唯一
         *      app：客户端唯一标识
         *      configs：指定配置监听的etcd的key的前缀
         */
        JanitorHelper janitorHelper = new JanitorHelper("/app/client", "client1", "client1.testConfig");

        janitorHelper
                // 添加事件监听器，指定event类型，当事件订阅到，可以在exec()处理自定义逻辑
                .addEventListener(
                        new AbstractEventListener(3, 10, false) {
                            @Override
                            public void exec(String eventValue) {
                                System.out.println("client1 收到消息：" + eventValue);
                            }

                            @Override
                            public EventTypeEnums event() {
                                return EventTypeEnums.CACHE_EXPIRE;
                            }
                        })
                // 添加配置监听器，当配置创建、读取、修改或者删除时，可以处理自定义逻辑
                .addProcessor(new ConfigProcessor() {
                    @Override
                    public void onLoad(Properties properties) {
                        for (Object key : properties.keySet()) {
                            System.out.println("client1 配置初始化： key：" + key + ", value：" + properties.get(key));
                        }
                    }

                    @Override
                    public void onAdd(Properties properties) {
                        for (Object key : properties.keySet()) {
                            System.out.println("client1 配置创建： key：" + key + ", value：" + properties.get(key));
                        }
                    }

                    @Override
                    public void onDelete(Properties properties) {
                        for (Object key : properties.keySet()) {
                            System.out.println("client1 配置删除： key：" + key + ", value：" + properties.get(key));
                        }                    }

                    @Override
                    public void onUpdate(Properties properties) {
                        for (Object key : properties.keySet()) {
                            System.out.println("client1 配置更新： key：" + key + ", value：" + properties.get(key));
                        }
                    }
                })
                // janitor-server 服务的端口号
                .setJanitorServerHostPort("6237")
                // janitor-server 服务的ip地址
                .setJanitorServerIp("127.0.0.1")
                .start();
        /*
         * client1往client2发送事件消息
         * send:
         *      eventType：事件类型，目前只实现CACHE_EXPIRE
         *      eventValue：事件具体内容，目标客户端可根据事件内容做具体业务逻辑
         *      targetAppNames：目标应用客户端，可以多个
         */
        janitorHelper.send(EventTypeEnums.CACHE_EXPIRE, "我发了个事件通知", "client2");
        Thread.sleep(1200000L);
    }

}
