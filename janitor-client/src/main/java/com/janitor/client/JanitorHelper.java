package com.janitor.client;

/**
 * ClassName JanitorHelper
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:16
 */
public class JanitorHelper extends RegistryService {
    /**
     * RegistryService的对外封装
     *
     * @param localPath 应用配置本地路径，设置唯一
     * @param app       应用名称，唯一
     * @param configs   相关配置的监听，key的前缀
     */
    public JanitorHelper(String localPath, String app, String... configs) {
        super(localPath, app, configs);
    }
}
