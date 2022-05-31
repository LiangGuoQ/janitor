package com.janitor.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.janitor.admin.entity.AppVersion;

/**
 * <p>
 * 应用版本表 服务类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-31
 */
public interface IAppVersionService extends IService<AppVersion> {

    /**
     * 获取应用最新版本号
     *
     * @param appName 应用名称
     * @return 版本号
     */
    Long getVersion(String appName);

    /**
     * 更新应用最新版本号
     *
     * @param appName 应用名称
     * @param version 最新版本号
     */
    void updateVersion(String appName, Long version);
}
