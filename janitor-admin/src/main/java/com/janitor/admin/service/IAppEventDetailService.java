package com.janitor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.janitor.admin.entity.AppEventDetail;
import com.janitor.admin.entity.query.AppEventDetailQuery;

/**
 * <p>
 * 应用事件明细 服务类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
public interface IAppEventDetailService extends IService<AppEventDetail> {

    /**
     * 分页查询
     *
     * @param query 查询对象
     * @return 分页结果
     */
    IPage<AppEventDetail> pageForList(AppEventDetailQuery query);

    /**
     * 定时任务逻辑
     *
     * @param detail 应用事件明细对象
     */
    void doSchedule(AppEventDetail detail);

}
