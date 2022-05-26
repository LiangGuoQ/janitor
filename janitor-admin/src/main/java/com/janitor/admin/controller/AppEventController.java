package com.janitor.admin.controller;

import com.janitor.admin.entity.dto.AppEventPushDTO;
import com.janitor.admin.entity.query.AppEventDetailQuery;
import com.janitor.admin.entity.query.AppEventQuery;
import com.janitor.admin.service.IAppEventDetailService;
import com.janitor.admin.service.IAppEventService;
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
 * 应用事件 前端控制器
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
@Api(tags = "应用事件")
@RestController
@RequestMapping("/v1/appEvent")
public class AppEventController {

    @Autowired
    private IAppEventService appEventService;

    @Autowired
    private IAppEventDetailService appEventDetailService;

    @ApiOperation("主动推送事件消息")
    @ApiImplicitParam(name = "dto", value = "主动推送事件消息body", dataType = "AppEventPushDTO")
    @PostMapping("/push")
    public Result push(@RequestBody AppEventPushDTO dto) {
        return Result.success(appEventService.push(dto));
    }

    @ApiOperation("获取推送事件列表")
    @ApiImplicitParam(name = "query", value = "获取推送事件列表body", dataType = "AppEventQuery")
    @PostMapping("/list")
    public Result list(@RequestBody AppEventQuery query) {
        return Result.success(appEventService.pageForList(query));
    }

    @ApiOperation("获取推送事件详细列表")
    @ApiImplicitParam(name = "query", value = "获取推送事件详细列表body", dataType = "AppEventDetailQuery")
    @PostMapping("/detail/list")
    public Result listDetails(@RequestBody AppEventDetailQuery query) {
        return Result.success(appEventDetailService.pageForList(query));
    }
}
