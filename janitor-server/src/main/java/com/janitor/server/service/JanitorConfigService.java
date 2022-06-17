package com.janitor.server.service;

import cn.hutool.core.io.FileUtil;
import com.janitor.common.etcd.EtcdEventKeyValueVo;
import com.janitor.common.etcd.EtcdEventVo;
import com.janitor.common.model.RegistryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ClassName ConfigAgentService
 * Description
 * 配置信息注册
 *
 * @author 曦逆
 * Date 2022/5/17 10:01
 */
@Service
public class JanitorConfigService extends AbstractRegisterService {
    private static final Logger log = LoggerFactory.getLogger(JanitorConfigService.class);

    @Autowired
    protected JanitorCacheService janitorCacheService;

    public JanitorConfigService() {
    }

    @PostConstruct
    private void init() {
        List<RegistryBean> registryBeans = this.janitorCacheService.getLocalCache();
        if (registryBeans != null) {
            registryBeans.forEach(this::register);
        }

    }

    @Override
    public void register(RegistryBean registryBean) {
        if (registryBean != null && registryBean.getData() != null) {
            RegistryBean oldRegistryBean = this.janitorCacheService.getRegistryBean(registryBean.getApp());
            if (oldRegistryBean != null) {
                oldRegistryBean.getData().forEach((key) -> this.etcdDao.getEtcdServiceV3().cancelWatch(key));
            }

            String configFilePath = registryBean.getLocalPath() + "/" + registryBean.getApp() + "/app.properties";
            Properties allProps = new Properties();
            registryBean.getData().forEach((key) -> {
                if (null != key && !"".equals(key)) {
                    String prefix;
                    if (key.endsWith(".*")) {
                        prefix = key.substring(0, key.length() - 1);
                    } else {
                        prefix = key;
                    }

                    this.etcdDao.getEtcdServiceV3().watch(key, prefix, (info) -> {
                        EtcdEventKeyValueVo kvs = info.getCurrent();
                        log.info("接收到配置变更推送，动作类型{},推送内容为{}", info.getEventType(), kvs);
                        Properties oldProperties = this.readProperties(configFilePath);
                        if (info.getEventType().equals(EtcdEventVo.EventType.DELETE)) {
                            oldProperties.remove(kvs.getKey());
                        } else {
                            oldProperties.put(kvs.getKey(), this.encode(kvs.getValue()));
                        }

                        try {
                            this.writeToProperties(this.propertiesToString(oldProperties), configFilePath);
                        } catch (Exception e) {
                            log.error("写入失败", e);
                        }

                    }, (error) -> log.error("监听出错", error), (complete) -> log.info("监听完成{}", complete));
                    if (key.endsWith(".*")) {
                        key = key.substring(0, key.length() - 2);
                    }

                    Map<String, String> configInfo = this.etcdDao.getEtcdServiceV3().getPrefix(key);
                    configInfo.forEach((configKey, value) -> allProps.put(configKey, this.encode(value)));
                } else {
                    throw new RuntimeException("监听key不能为空");
                }
            });

            try {
                this.writeToProperties(this.propertiesToString(allProps), configFilePath);
            } catch (Exception e) {
                log.error("配置拉取后，写入配置失败", e);
            }

        } else {
            throw new RuntimeException("未指定配置项");
        }
    }

    private void writeToProperties(String configInfo, String path) {
        FileUtil.writeString(configInfo, new File(path), StandardCharsets.UTF_8);
    }

    private String propertiesToString(Properties properties) {
        StringBuilder str = new StringBuilder();
        properties.forEach((key, value) -> str.append(key).append("=").append(value).append("\r\n"));
        return str.toString();
    }

    private Properties readProperties(String path) {
        File file = new File(path);
        Properties properties = new Properties();
        if (file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                log.error("读取配置失败", e);
            }
        }

        return properties;
    }

    private String encode(Object str) {
        return null != str && !"".equals(str) ? new String(Base64.getEncoder().encode(str.toString().getBytes()), StandardCharsets.UTF_8) : "";
    }
}
