package com.janitor.admin.controller;

import com.janitor.admin.entity.query.AppConfHisQuery;
import com.janitor.admin.service.IAppConfHisService;
import com.janitor.common.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 应用配置历史记录表 前端控制器
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
@Api(tags = "应用配置历史记录接口")
@RestController
@RequestMapping("/v1/appConfHis")
public class AppConfHisController {

    @Autowired
    private IAppConfHisService iAppConfHisService;

    @ApiOperation("获取配置历史记录列表")
    @ApiImplicitParam(name = "query", value = "获取推送事件列表body", dataType = "AppConfHisQuery")
    @PostMapping("/list")
    public Result list(@RequestBody AppConfHisQuery query) {
        return Result.success(iAppConfHisService.pageForList(query));
    }

}
