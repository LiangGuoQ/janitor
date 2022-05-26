package com.janitor.common.etcd.annotation;

import com.janitor.common.etcd.dao.EtcdDao;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName EnableEtcd
 * Description
 *
 * @author lianggq4
 * Date 2022/5/25 9:12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EtcdDao.class)
public @interface EnableEtcd {
}
