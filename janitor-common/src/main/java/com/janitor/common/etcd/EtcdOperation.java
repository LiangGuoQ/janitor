package com.janitor.common.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName EtcdOperation
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtcdOperation {
    /**
     * key
     */
    private String key;
    /**
     * value
     */
    private String value;
    /**
     * 操作类型
     */
    private EtcdOperation.OperateType operateType;

    public enum OperateType {
        /**
         * 操作类型
         */
        PUT,
        DELETE;

        OperateType() {
        }
    }
}
