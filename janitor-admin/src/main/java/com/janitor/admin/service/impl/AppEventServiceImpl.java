package com.janitor.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppEvent;
import com.janitor.admin.entity.AppEventDetail;
import com.janitor.admin.entity.dto.AppEventPushDTO;
import com.janitor.admin.entity.query.AppEventQuery;
import com.janitor.admin.mapper.AppEventMapper;
import com.janitor.admin.service.IAppEventDetailService;
import com.janitor.admin.service.IAppEventService;
import com.janitor.common.etcd.EtcdOperation;
import com.janitor.common.etcd.dao.EtcdDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

import static com.janitor.common.constant.EventConstants.EVENT_KEY_PREFIX;
import static com.janitor.common.constant.EventConstants.EVENT_KEY_VALUE_FORMAT;

/**
 * <p>
 * 应用事件 服务实现类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
@Slf4j
@Service
public class AppEventServiceImpl extends ServiceImpl<AppEventMapper, AppEvent> implements IAppEventService {

    @Autowired
    private IAppEventDetailService appEventDetailService;

    @Autowired
    private EtcdDao etcdDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long push(AppEventPushDTO dto) {
        AppEvent appEvent = dto.toEvent();
        this.getBaseMapper().insert(appEvent);
        List<AppEventDetail> eventDetailList = dto.toEventDetailList(appEvent.getId());
        this.appEventDetailService.saveBatch(eventDetailList);
        try {
            String keyValue = String.format(EVENT_KEY_VALUE_FORMAT, dto.getEventType(), dto.getEventValue());
            List<EtcdOperation> operationList = dto.getTargetIpList()
                    .stream()
                    .map((ip) -> {
                        String etcdKey = EVENT_KEY_PREFIX + dto.getAppName() + "." + ip + "." + appEvent.getId();
                        return new EtcdOperation(etcdKey, keyValue, EtcdOperation.OperateType.PUT);
                    })
                    .collect(Collectors.toList());
            boolean pushResult = etcdDao.getEtcdServiceV3().batchOperate(operationList);
            Assert.isTrue(pushResult, "往ETCD推送消息失败");
        } catch (Exception e) {
            log.error("往ETCD推送消息失败", e);
            throw e;
        }
        return appEvent.getId();
    }

    @Override
    public IPage<AppEvent> pageForList(AppEventQuery query) {
        IPage<AppEvent> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<AppEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(query.getAppName()), AppEvent::getAppName, query.getAppName())
                .orderByDesc(AppEvent::getId);
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }
}
