package com.janitor.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName JanitorServer
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 17:34
 */
@Slf4j
@SpringBootApplication
public class JanitorServer {

    public static void main(String[] args) {
        try {
            SpringApplication.run(JanitorServer.class, args);
        } catch (Throwable e) {
            log.error("项目启动失败", e);
        }
    }

}
