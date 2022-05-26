package com.janitor.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppEventDetail;
import com.janitor.admin.entity.query.AppEventDetailQuery;
import com.janitor.admin.mapper.AppEventDetailMapper;
import com.janitor.admin.service.IAppEventDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用事件明细 服务实现类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
@Service
public class AppEventDetailServiceImpl extends ServiceImpl<AppEventDetailMapper, AppEventDetail> implements IAppEventDetailService {

    @Override
    public IPage<AppEventDetail> pageForList(AppEventDetailQuery query) {
        IPage<AppEventDetail> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<AppEventDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(query.getEventId()), AppEventDetail::getEventId, query.getEventId());
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }
}
