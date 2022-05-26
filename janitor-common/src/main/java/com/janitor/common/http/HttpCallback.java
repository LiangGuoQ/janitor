package com.janitor.common.http;

/**
 * ClassName HttpCallback
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:22
 */
public interface HttpCallback {
    /**
     * 请求失败执行方法
     *
     * @param e 异常
     */
    default void failed(Exception e) {
    }

    /**
     * 处理成功执行方法
     *
     * @param r 结果
     */
    void completed(R r);

    /**
     * 取消
     */
    default void cancelled() {
    }
}
