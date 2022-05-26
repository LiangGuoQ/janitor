package com.janitor.common.etcd.dao;

import cn.hutool.core.util.ObjectUtil;
import com.janitor.common.etcd.EtcdServiceV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * ClassName EtcdDao
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 8:50
 */
@Component
public class EtcdDao {

    private EtcdServiceV3 etcdServiceV3;

    @Value("${janitor.etcd.server}")
    private String janitorEtcdService;

    @Value("${janitor.etcd.authority}")
    private Boolean janitorEtcdAuthority;

    @Value("${janitor.etcd.user}")
    private String janitorEtcdUser;

    @Value("${janitor.etcd.password}")
    private String janitorEtcdPassword;

    public EtcdDao() {
    }

    @PostConstruct
    public void init() {
        String[] urls = Objects.requireNonNull(this.janitorEtcdService).split(",");
        if (ObjectUtil.equal(this.janitorEtcdAuthority, Boolean.TRUE)) {
            this.etcdServiceV3 = new EtcdServiceV3(this.janitorEtcdUser, this.janitorEtcdPassword, urls);
        } else {
            this.etcdServiceV3 = new EtcdServiceV3(urls);
        }

    }

    public EtcdServiceV3 getEtcdServiceV3() {
        return this.etcdServiceV3;
    }

}
