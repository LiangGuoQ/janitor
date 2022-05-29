package com.janitor.admin.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.janitor.common.etcd.annotation.EnableEtcd;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName JanitorAdminConfig
 * Description
 *
 * @author 曦逆
 * Date 2022/5/24 16:59
 */
@EnableEtcd
@Configuration
@MapperScan("com.janitor.admin.mapper")
public class JanitorAdminConfig {

    /**
     * mybatisPlus分页插件
     *
     * @return 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

}
