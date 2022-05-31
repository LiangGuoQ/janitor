package com.janitor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.janitor.admin.entity.AppConfHis;
import com.janitor.admin.entity.query.AppConfHisQuery;

/**
 * <p>
 * 应用配置历史记录表 服务类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
public interface IAppConfHisService extends IService<AppConfHis> {

    /**
     * 分页查询
     *
     * @param query 查询对象
     * @return 分页结果
     */
    IPage<AppConfHis> pageForList(AppConfHisQuery query);

}
