package com.janitor.admin.scheduled;

import com.janitor.admin.entity.AppEventDetail;
import com.janitor.admin.enums.AppEventPushResultEnum;
import com.janitor.admin.service.IAppEventDetailService;
import com.janitor.admin.utils.RedisKeysUtil;
import com.janitor.common.etcd.dao.EtcdDao;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName AppEventSchedule
 * Description 定时任务，后续迁移分布式定时任务
 *
 * @author 曦逆
 * Date 2022/6/1 13:13
 */
@Slf4j
@Component
@EnableScheduling
public class AppEventSchedule {

    /**
     * 最大等待锁时间
     */
    private static final long SCHEDULE_MAX_WAIT_LOCK_TIME = 10L;

    /**
     * 最大等待锁自动释放时间
     */
    private static final long SCHEDULE_MAX_WAIT_RELEASE_TIME = 120000L;

    @Autowired
    private IAppEventDetailService iAppEventDetailService;

    @Autowired
    private EtcdDao etcdDao;

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(fixedRate = 2000)
    public void scheduler() {
        List<AppEventDetail> detailList = iAppEventDetailService.lambdaQuery()
                .select(AppEventDetail::getId)
                .eq(AppEventDetail::getPushResult, AppEventPushResultEnum.UNKNOWN.getCode())
                .list();
        for (AppEventDetail detail : detailList) {
            RLock lock = redissonClient.getLock(RedisKeysUtil.getScheduleLockKey(detail.getId()));
            try {
                if (lock.tryLock(SCHEDULE_MAX_WAIT_LOCK_TIME, SCHEDULE_MAX_WAIT_RELEASE_TIME, TimeUnit.MILLISECONDS)) {
                    iAppEventDetailService.doSchedule(detail);
                } else {
                    log.warn("当前事件明细正在处理");
                }
            } catch (InterruptedException e) {
                log.error("lock error", e);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("UNLOCK FAILED: key={}", lock.getName(), e);
                }
            }
        }
    }

}
