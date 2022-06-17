package com.janitor.server.service;

import com.janitor.common.model.RegistryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName RegisterHelper
 * Description 责任链工具类
 *
 * @author 曦逆
 * Date 2022/6/17 13:28
 */
@Component
public class RegisterHelper {

    @Autowired
    private JanitorConfigService janitorConfigService;

    @Autowired
    private JanitorEventService janitorEventService;

    @Autowired
    private JanitorCacheService janitorCacheService;

    /**
     * 责任链，链表
     */
    private List<AbstractRegisterService> registerList;

    @PostConstruct
    private void init() {
        registerList = new ArrayList<>();
        registerList.add(janitorConfigService);
        registerList.add(janitorEventService);
        registerList.add(janitorCacheService);
    }

    /**
     * 执行责任链
     *
     * @param registryBean 上下文内容
     */
    public void process(RegistryBean registryBean) {
        // 前置构建和检查
        preCheck(registryBean);

        //  遍历执行
        for (AbstractRegisterService registerService : registerList) {
            registerService.register(registryBean);
        }
    }

    /**
     * 前置构建和检查
     *
     * @param registryBean 上下文内容
     */
    private void preCheck(RegistryBean registryBean) {

    }
}
