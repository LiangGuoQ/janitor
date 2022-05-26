package com.janitor.server.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.janitor.common.model.RegistryBean;
import com.janitor.common.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName RegistryCacheService
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 8:58
 */
@Component
public class RegistryCacheService {
    private static final Logger log = LoggerFactory.getLogger(RegistryCacheService.class);
    private List<RegistryBean> localCache;
    private String localIp;
    private static final ConcurrentHashMap<String, RegistryBean> REGISTRY_BEAN_HOLDER = new ConcurrentHashMap<>();
    private static final String REG_CACHE_KEY = "janitor.registry.cache";
    @Autowired
    private Environment env;

    public RegistryCacheService() {
    }

    @PostConstruct
    public void readLocal() {
        String configLocation = this.env.getProperty(REG_CACHE_KEY);
        Assert.notNull(configLocation, REG_CACHE_KEY + "can not found");
        File registryCacheFile = new File(configLocation);
        String[] realLocalIp = IpUtil.getRealLocalIp();
        Arrays.sort(realLocalIp);
        this.localIp = String.join(",", realLocalIp);
        if (registryCacheFile.exists()) {
            try {
                String content = FileUtil.readString(registryCacheFile, StandardCharsets.UTF_8);
                String cnt = StrUtil.trim(content);
                if (cnt.length() > 0) {
                    this.localCache = JSONUtil.toList(content, RegistryBean.class);
                }
            } catch (Exception e) {
                log.error("配置服务登记文件出错，请检查", e);
            }
        }

    }

    public List<RegistryBean> getLocalCache() {
        return this.localCache;
    }

    public RegistryBean getRegistryBean(String appName) {
        return REGISTRY_BEAN_HOLDER.get(appName);
    }

    public void addRegistryBean(String appName, RegistryBean registryBean) {
        REGISTRY_BEAN_HOLDER.put(appName, registryBean);
    }

    public void flushToLocalCache() {
        try {
            FileUtil.writeString(JSONUtil.toJsonStr(REGISTRY_BEAN_HOLDER.values()), new File(Objects.requireNonNull(this.env.getProperty(REG_CACHE_KEY))), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("注册信息后，写入本地缓存失败", e);
        }

    }

    public String getLocalIp() {
        return this.localIp;
    }
}
