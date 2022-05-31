package com.janitor.admin.entity.bo;

import com.janitor.common.etcd.EtcdOperation;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ClassName AppConfRefreshBO
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 10:25
 */
@Data
@Builder
public class AppConfRefreshBO {

    private Integer addCount;

    private Integer deleteCount;

    private Integer updateCount;

    private List<EtcdOperation> etcdOperations;
}
