package com.janitor.admin.controller;

import com.janitor.admin.service.IAppVersionService;
import com.janitor.common.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 应用版本表 前端控制器
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-31
 */
@Api(tags = "应用配置版本接口")
@RestController
@RequestMapping("/v1/appVersion")
public class AppVersionController {

    @Autowired
    private IAppVersionService iAppVersionService;

    @ApiOperation("获取应用历史版本号列表")
    @ApiImplicitParam(name = "appName", value = "应用名称", dataType = "String")
    @GetMapping("/list")
    public Result listAppVersion(String appName) {
        Long version = iAppVersionService.getVersion(appName);
        List<Long> versionList = Stream.iterate(version - 1, (item) -> item - 1L).limit(version).collect(Collectors.toList());
        return Result.success(versionList);
    }

}
