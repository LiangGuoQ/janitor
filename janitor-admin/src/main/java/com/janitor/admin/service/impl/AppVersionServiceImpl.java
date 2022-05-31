package com.janitor.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppVersion;
import com.janitor.admin.mapper.AppVersionMapper;
import com.janitor.admin.service.IAppVersionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * <p>
 * 应用版本表 服务实现类
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-31
 */
@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements IAppVersionService {

    @Override
    public Long getVersion(String appName) {
        Optional<AppVersion> optional = this.lambdaQuery()
                .eq(AppVersion::getAppName, appName)
                .oneOpt();
        return optional.map(appVersion -> Optional.ofNullable(appVersion.getVersion()).orElse(0L)).orElse(0L) + 1;
    }

    @Override
    public void updateVersion(String appName, Long version) {
        Optional<AppVersion> optional = this.lambdaQuery()
                .eq(AppVersion::getAppName, appName)
                .oneOpt();
        AppVersion appVersion = optional.orElseGet(() -> AppVersion.builder().appName(appName).build());
        appVersion.setVersion(version);
        this.saveOrUpdate(appVersion);
    }
}
