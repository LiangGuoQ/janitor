package com.janitor.admin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName JanitorAdmin
 * Description
 *
 * @author 曦逆
 * Date 2022/5/24 16:57
 */
@Slf4j
@SpringBootApplication
public class JanitorAdmin {

    public static void main(String[] args) {
        try {
            SpringApplication.run(JanitorAdmin.class, args);
        } catch (Throwable e) {
            log.error("项目启动失败", e);
        }
    }

}
