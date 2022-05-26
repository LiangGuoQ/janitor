package com.janitor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.janitor.admin.entity.dto.AppEventPushDTO;
import com.janitor.admin.entity.AppEvent;
import com.janitor.admin.entity.query.AppEventQuery;

/**
 * <p>
 * 应用事件 服务类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
public interface IAppEventService extends IService<AppEvent> {

    /**
     * 事件推送
     *
     * @param dto 事件推送dto
     * @return 主记录ID
     */
    Long push(AppEventPushDTO dto);

    /**
     * 分页查询
     *
     * @param query 查询对象
     * @return 分页结果
     */
    IPage<AppEvent> pageForList(AppEventQuery query);
}
