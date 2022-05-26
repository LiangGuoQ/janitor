package com.janitor.common.etcd;

import io.etcd.jetcd.KeyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * ClassName EtcdEventKeyValueVo
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:11
 */
@Data
@Builder
@AllArgsConstructor
public class EtcdEventKeyValueVo {
    /**
     * key
     */
    private String key;
    /**
     * value
     */
    private String value;
    /**
     * 创建修订版本
     */
    private long createRevision;
    /**
     * 修改版本
     */
    private long modRevision;
    /**
     * 版本
     */
    private long version;
    /**
     * 租赁，key的有效期
     */
    private long lease;

    private EtcdEventKeyValueVo(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "EtcdEventKeyValueVo{key='" + this.key + '\'' + ", value='" + this.value + '\'' + ", createRevision=" + this.createRevision + ", lease=" + this.lease + ", modRevision=" + this.modRevision + ", version=" + this.version + '}';
    }

    public static EtcdEventKeyValueVo valueOf(KeyValue keyValue) {
        EtcdEventKeyValueVo vo = new EtcdEventKeyValueVo(Utils.bsToStr(keyValue.getKey()), Utils.bsToStr(keyValue.getValue()));
        vo.setCreateRevision(keyValue.getCreateRevision());
        vo.setLease(keyValue.getLease());
        vo.setModRevision(keyValue.getModRevision());
        vo.setVersion(keyValue.getVersion());
        return vo;
    }
}
