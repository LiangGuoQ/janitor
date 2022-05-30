package com.janitor.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.janitor.admin.entity.AppConf;
import com.janitor.admin.mapper.AppConfMapper;
import com.janitor.admin.service.IAppConfService;
import org.springframework.stereotype.Service;

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

}
