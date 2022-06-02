package com.janitor.admin.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppEventDetail;
import com.janitor.admin.entity.query.AppEventDetailQuery;
import com.janitor.admin.enums.AppEventPushResultEnum;
import com.janitor.admin.mapper.AppEventDetailMapper;
import com.janitor.admin.service.IAppEventDetailService;
import com.janitor.common.etcd.dao.EtcdDao;
import com.janitor.common.json.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.janitor.common.constant.EventConstants.EVENT_KEY_PREFIX;
import static com.janitor.common.constant.EventConstants.EVENT_RESPONSE;

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

    @Autowired
    private EtcdDao etcdDao;

    @Override
    public IPage<AppEventDetail> pageForList(AppEventDetailQuery query) {
        IPage<AppEventDetail> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<AppEventDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(query.getEventId()), AppEventDetail::getEventId, query.getEventId())
                .orderByDesc(AppEventDetail::getId);
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doSchedule(AppEventDetail detail) {
        detail = this.getById(detail.getId());
        if (ObjectUtil.equal(detail.getPushResult(), AppEventPushResultEnum.UNKNOWN.getCode())) {
            String etcdKey = EVENT_KEY_PREFIX + detail.getAppName() + "." + detail.getTargetIp() + "." + detail.getEventId();
            String eventValue = etcdDao.getEtcdServiceV3().get(etcdKey);
            if (StrUtil.isNotBlank(eventValue)) {
                if (StrUtil.contains(eventValue, EVENT_RESPONSE)) {
                    Map<String, Object> eventValueMap = JsonUtil.parseMap(eventValue, String.class, Object.class);
                    Map<String, Object> responseMap = JsonUtil.parseMap(JsonUtil.toJson(eventValueMap.get(EVENT_RESPONSE)), String.class, Object.class);
                    Integer code = Convert.toInt(responseMap.get("code"));
                    detail.setPushResult(ObjectUtil.equal(code, 0) ? AppEventPushResultEnum.SUCCESS.getCode() : AppEventPushResultEnum.FAIL.getCode());
                    etcdDao.getEtcdServiceV3().delete(etcdKey);
                } else {
                    if (detail.getRetryActive() < detail.getRetryPlan()) {
                        detail.setRetryActive(detail.getRetryActive() + 1);
                        etcdDao.getEtcdServiceV3().put(etcdKey, eventValue);
                    } else {
                        detail.setPushResult(AppEventPushResultEnum.FAIL.getCode());
                        etcdDao.getEtcdServiceV3().delete(etcdKey);
                    }
                }
                this.updateById(detail);
            }
        }
    }
}
