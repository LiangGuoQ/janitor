package com.janitor.server.controller;

import com.janitor.common.base.Result;
import com.janitor.common.model.*;
import com.janitor.server.service.JanitorConfigService;
import com.janitor.server.service.JanitorEventService;
import com.janitor.server.service.RegistryCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * ClassName ApiRestController
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 10:14
 */
@Controller
@RequestMapping({"/api"})
public class ApiRestController {
    @Autowired
    private JanitorConfigService janitorConfigService;
    @Autowired
    private JanitorEventService janitorEventService;
    @Autowired
    private RegistryCacheService registryCacheService;

    @ResponseBody
    @RequestMapping({"/registry"})
    public Result registry(@RequestBody RegistryBean registryBean) {
        this.janitorConfigService.registerConfig(registryBean);
        this.janitorEventService.registerEvent(registryBean);
        this.registryCacheService.addRegistryBean(registryBean.getApp(), registryBean);
        this.registryCacheService.flushToLocalCache();
        return Result.success();
    }

    @ResponseBody
    @RequestMapping({"/notify"})
    public Result notify(@RequestBody EventResultNotifyReq req) {
        boolean notify = this.janitorEventService.notify(req);
        return Result.success(notify ? "消息确认录入成功" : "消息确认录入失败");
    }

    @ResponseBody
    @RequestMapping({"/event"})
    public EventPushResult eventSend(@RequestBody EventPushReq req) {
        return this.janitorEventService.pushEvent(req);
    }

    @ResponseBody
    @PostMapping({"/heartbeat"})
    public Result heartbeat(@RequestBody @Valid HeartbeatDTO heartbeatDTO) {
        boolean success = this.janitorEventService.heartBeat(heartbeatDTO);
        return Result.success(success ? "心跳录入成功" : "心跳录入失败");
    }
}
