package com.janitor.admin.service;

import java.util.List;
import java.util.Set;

/**
 * ClassName AppService
 * Description
 *
 * @author 曦逆
 * Date 2022/5/26 13:41
 */
public interface AppService {

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    Set<String> getAppNameList();

    /**
     * 获取应用心跳维护的ip列表
     *
     * @param appName 应用名称
     * @return 应用服务器ip列表
     */
    List<String> getIpList(String appName);
}
