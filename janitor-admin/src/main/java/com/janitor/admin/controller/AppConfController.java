package com.janitor.admin.controller;

import com.janitor.admin.entity.AppConf;
import com.janitor.admin.entity.bo.AppConfRefreshBO;
import com.janitor.admin.entity.dto.AppConfRefreshDTO;
import com.janitor.admin.entity.query.AppConfQuery;
import com.janitor.admin.service.IAppConfService;
import com.janitor.admin.utils.RedisKeysUtil;
import com.janitor.common.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 应用配置 前端控制器
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
@Slf4j
@Api(tags = "应用配置接口")
@RestController
@RequestMapping("/v1/appConf")
public class AppConfController {

    @Autowired
    private IAppConfService iAppConfService;

    @Autowired
    private RedissonClient redissonClient;

    @ApiOperation("获取配置列表")
    @ApiImplicitParam(name = "query", value = "获取配置列表body", dataType = "AppConfQuery")
    @PostMapping("/list")
    public Result list(@RequestBody AppConfQuery query) {
        return Result.success(iAppConfService.pageForList(query));
    }

    @ApiOperation("添加配置")
    @ApiImplicitParam(name = "appConf", value = "添加配置body", dataType = "AppConf")
    @PostMapping("/add")
    public Result add(@RequestBody AppConf appConf) {
        iAppConfService.save(appConf);
        return Result.success(appConf.getId());

    }

    @ApiOperation("修改配置")
    @ApiImplicitParam(name = "appConf", value = "修改配置body", dataType = "AppConf")
    @PostMapping("/update")
    public Result update(@RequestBody AppConf appConf) {
        iAppConfService.update(appConf);
        return Result.success();
    }

    @ApiOperation("删除配置")
    @ApiImplicitParam(name = "id", paramType = "path", value = "主键ID", dataType = "Long")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        iAppConfService.delete(id);
        return Result.success();
    }

    @ApiOperation("发布配置")
    @ApiImplicitParam(name = "dto", value = "发布配置body", dataType = "AppConfRefreshDTO")
    @PostMapping("/refresh")
    public Result refresh(@RequestBody AppConfRefreshDTO dto) {
        RLock lock = redissonClient.getLock(RedisKeysUtil.getRefreshLockKey(dto.getAppName()));
        try {
            if (lock.tryLock(200L, 120000L, TimeUnit.MILLISECONDS)) {
                AppConfRefreshBO bo = iAppConfService.refresh(dto);
                return Result.success(bo);
            } else {
                return Result.fail("当前应用正在发布,请稍后再操作");
            }
        } catch (InterruptedException e) {
            log.error("lock error", e);
            return Result.fail("发布配置异常,请稍后再操作");
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("UNLOCK FAILED: key={}", RedisKeysUtil.getRefreshLockKey(dto.getAppName()), e);
            }
        }
    }

}
