package com.janitor.common.etcd;

/**
 * ClassName WatchAction
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:27
 */
public interface WatchAction<T> {
    /**
     * 处理
     *
     * @param t 数据
     */
    void handler(T t);
}
