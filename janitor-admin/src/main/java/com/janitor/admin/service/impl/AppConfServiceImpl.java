package com.janitor.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppConf;
import com.janitor.admin.entity.AppConfHis;
import com.janitor.admin.entity.bo.AppConfRefreshBO;
import com.janitor.admin.entity.dto.AppConfRefreshDTO;
import com.janitor.admin.entity.query.AppConfQuery;
import com.janitor.admin.enums.AppConfStatusEnum;
import com.janitor.admin.exception.JanitorAdminException;
import com.janitor.admin.mapper.AppConfMapper;
import com.janitor.admin.service.IAppConfHisService;
import com.janitor.admin.service.IAppConfService;
import com.janitor.admin.service.IAppVersionService;
import com.janitor.common.etcd.EtcdOperation;
import com.janitor.common.etcd.dao.EtcdDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 应用配置 服务实现类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
@Service
public class AppConfServiceImpl extends ServiceImpl<AppConfMapper, AppConf> implements IAppConfService {

    @Autowired
    private EtcdDao etcdDao;

    @Autowired
    private IAppConfHisService iAppConfHisService;

    @Autowired
    private IAppVersionService iAppVersionService;

    @Override
    public IPage<AppConf> pageForList(AppConfQuery query) {
        IPage<AppConf> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<AppConf> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(query.getAppName()), AppConf::getAppName, query.getAppName())
                .eq(StrUtil.isNotBlank(query.getConfKey()), AppConf::getConfKey, query.getConfKey())
                .orderByDesc(AppConf::getId);
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }

    @Override
    public void update(AppConf appConf) {
        AppConf conf = this.getById(appConf.getId());

        if (StrUtil.equals(appConf.getConfValue(), conf.getConfValue())) {
            throw JanitorAdminException.of("配置内容无变化，不需要更新");
        }

        conf.setConfKey(appConf.getConfKey());
        conf.setConfValue(appConf.getConfValue());
        conf.setRemark(appConf.getRemark());
        if (ObjectUtil.equal(conf.getStatus(), AppConfStatusEnum.PUBLISHED.getCode())) {
            conf.setStatus(AppConfStatusEnum.PUBLISHED_MODIFY.getCode());
        } else {
            conf.setStatus(AppConfStatusEnum.MODIFY.getCode());
        }
        this.updateById(conf);
    }

    @Override
    public void delete(Long id) {
        AppConf conf = this.getById(id);
        if (ObjectUtil.equal(conf.getStatus(), AppConfStatusEnum.PUBLISHED.getCode())) {
            conf.setStatus(AppConfStatusEnum.PUBLISHED_DELETE.getCode());
        } else {
            conf.setStatus(AppConfStatusEnum.DELETE.getCode());
        }
        this.updateById(conf);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppConfRefreshBO refresh(AppConfRefreshDTO dto) {
        String appName = dto.getAppName();

        // 获取配置列表，过滤未发布删除状态的
        List<AppConf> confList = this.lambdaQuery()
                .eq(AppConf::getAppName, appName)
                .ne(AppConf::getStatus, AppConfStatusEnum.DELETE.getCode())
                .list();

        // 获取应用版本号
        Long version = iAppVersionService.getVersion(appName);

        // 组装历史版本数据
        List<AppConfHis> hisList = convert2HisEntity(confList, version);

        // 组装业务bo对象
        AppConfRefreshBO appConfRefreshBO = convert2RefreshBo(confList);
        if (CollectionUtil.isEmpty(appConfRefreshBO.getEtcdOperations())) {
            throw JanitorAdminException.of("没有更新内容,无需发布");
        }

        // 保存历史版本
        iAppConfHisService.saveBatch(hisList);

        // 更新最新版本
        iAppVersionService.updateVersion(appName, version);

        // 删除未发布删除状态和发布删除状态的配置
        QueryWrapper<AppConf> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AppConf::getAppName, appName)
                .in(AppConf::getStatus, AppConfStatusEnum.DELETE.getCode(), AppConfStatusEnum.PUBLISHED_DELETE.getCode());
        this.baseMapper.delete(queryWrapper);

        // 更新为已发布状态
        UpdateWrapper<AppConf> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(AppConf::getStatus, AppConfStatusEnum.PUBLISHED.getCode())
                .eq(AppConf::getAppName, appName);
        this.update(updateWrapper);

        // 发布到ETCD
        boolean batchOperateResult = etcdDao.getEtcdServiceV3().batchOperate(appConfRefreshBO.getEtcdOperations());
        if (!batchOperateResult) {
            throw JanitorAdminException.of("发布失败，请联系管理员");
        }

        appConfRefreshBO.setEtcdOperations(null);
        return appConfRefreshBO;
    }

    private List<AppConfHis> convert2HisEntity(List<AppConf> confList, Long version) {
        List<AppConfHis> hisList = new ArrayList<>();
        for (AppConf appConf : confList) {
            AppConfHis confHis = AppConfHis.builder()
                    .appName(appConf.getAppName())
                    .confKey(appConf.getConfKey())
                    .confValue(appConf.getConfValue())
                    .version(version)
                    .remark(appConf.getRemark())
                    .build();
            hisList.add(confHis);
        }
        return hisList;
    }

    private AppConfRefreshBO convert2RefreshBo(List<AppConf> confList) {
        List<EtcdOperation> result = new ArrayList<>(confList.size());
        int addCount = 0;
        int deleteCount = 0;
        int updateCount = 0;
        for (AppConf appConf : confList) {
            switch (AppConfStatusEnum.getFromCode(appConf.getStatus())) {
                case PUBLISHED_DELETE:
                    ++deleteCount;
                    result.add(new EtcdOperation(appConf.getAppName().concat(".").concat(appConf.getConfKey()), appConf.getConfValue(), EtcdOperation.OperateType.DELETE));
                    break;
                case CREATE:
                case MODIFY:
                    result.add(new EtcdOperation(appConf.getAppName().concat(".").concat(appConf.getConfKey()), appConf.getConfValue(), EtcdOperation.OperateType.PUT));
                    ++addCount;
                    break;
                case PUBLISHED_MODIFY:
                    result.add(new EtcdOperation(appConf.getAppName().concat(".").concat(appConf.getConfKey()), appConf.getConfValue(), EtcdOperation.OperateType.PUT));
                    ++updateCount;
                default:
                    break;
            }
        }
        return AppConfRefreshBO.builder()
                .addCount(addCount)
                .deleteCount(deleteCount)
                .updateCount(updateCount)
                .etcdOperations(result)
                .build();
    }
}
