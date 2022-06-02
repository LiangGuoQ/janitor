package com.janitor.admin.utils;

/**
 * ClassName RedisKeysUtil
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 10:31
 */
public class RedisKeysUtil {

    public static String getRefreshLockKey(String appName) {
        return "app:conf:lock:" + appName;
    }

    public static String getScheduleLockKey(Long eventDetailId) {
        return "app:schedule:lock:" + eventDetailId;
    }
}
