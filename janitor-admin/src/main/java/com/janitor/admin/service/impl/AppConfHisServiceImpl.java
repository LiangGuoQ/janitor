package com.janitor.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppConfHis;
import com.janitor.admin.entity.query.AppConfHisQuery;
import com.janitor.admin.mapper.AppConfHisMapper;
import com.janitor.admin.service.IAppConfHisService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用配置历史记录表 服务实现类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
@Service
public class AppConfHisServiceImpl extends ServiceImpl<AppConfHisMapper, AppConfHis> implements IAppConfHisService {

    @Override
    public IPage<AppConfHis> pageForList(AppConfHisQuery query) {
        IPage<AppConfHis> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<AppConfHis> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(query.getAppName()), AppConfHis::getAppName, query.getAppName())
                .eq(StrUtil.isNotBlank(query.getConfKey()), AppConfHis::getConfKey, query.getConfKey())
                .eq(ObjectUtil.isNotNull(query.getVersion()), AppConfHis::getVersion, query.getVersion())
                .orderByDesc(AppConfHis::getId);
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }
}
