package com.janitor.server.service;

import com.janitor.common.etcd.dao.EtcdDao;
import com.janitor.common.model.RegistryBean;
import com.janitor.server.util.SnowflakeIdWorkerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * ClassName AbstractRegisterService
 * Description 抽象注册类-定义统一方法，公共属性，公共方法
 *
 * @author 曦逆
 * Date 2022/6/17 13:18
 */
public abstract class AbstractRegisterService {

    @Autowired
    protected Environment env;
    @Autowired
    protected EtcdDao etcdDao;
    @Resource
    protected SnowflakeIdWorkerUtil snowflakeIdWorkerUtil;

    /**
     * 统一注册入口
     *
     * @param registryBean 入参
     */
    protected abstract void register(RegistryBean registryBean);

}
