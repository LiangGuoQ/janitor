package com.janitor.admin.controller;

import com.janitor.admin.service.AppService;
import com.janitor.common.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 应用通用接口 前端控制器
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
@Api(tags = "应用通用接口")
@RestController
@RequestMapping("/v1/app")
public class AppController {

    @Autowired
    private AppService appService;

    @ApiOperation("获取应用列表")
    @GetMapping("/list")
    public Result listApps() {
        return Result.success(appService.getAppNameList());
    }

    @ApiOperation("获取指定应用的ip列表")
    @ApiImplicitParam(name = "appName", paramType = "query", value = "应用名称", dataType = "String")
    @GetMapping("/ip/list")
    public Result listAppIps(String appName) {
        return Result.success(appService.getIpList(appName));
    }
}
