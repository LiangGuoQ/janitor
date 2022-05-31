package com.janitor.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.janitor.admin.entity.AppConf;
import com.janitor.admin.entity.bo.AppConfRefreshBO;
import com.janitor.admin.entity.dto.AppConfRefreshDTO;
import com.janitor.admin.entity.query.AppConfQuery;

/**
 * <p>
 * 应用配置 服务类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
public interface IAppConfService extends IService<AppConf> {

    /**
     * 分页查询
     *
     * @param query 查询对象
     * @return 分页结果
     */
    IPage<AppConf> pageForList(AppConfQuery query);


    /**
     * 更新配置信息
     *
     * @param appConf 配置信息
     */
    void update(AppConf appConf);

    /**
     * 删除配置信息
     *
     * @param id 主键ID
     */
    void delete(Long id);

    /**
     * 发布配置到配置中心，下发给janitor-client
     *
     * @param dto 发布对象
     * @return 发布结果业务逻辑对象
     */
    AppConfRefreshBO refresh(AppConfRefreshDTO dto);
}
