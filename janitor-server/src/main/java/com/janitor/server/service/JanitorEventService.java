package com.janitor.server.service;

import com.janitor.common.enums.EventTypeEnums;
import com.janitor.common.etcd.EtcdEventKeyValueVo;
import com.janitor.common.etcd.EtcdEventVo;
import com.janitor.common.etcd.EtcdOperation;
import com.janitor.common.etcd.dao.EtcdDao;
import com.janitor.common.json.JsonUtil;
import com.janitor.common.model.*;
import com.janitor.common.util.EventLogWriter;
import com.janitor.server.util.SnowflakeIdWorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.janitor.common.constant.EventConstants.*;

/**
 * ClassName EventAgentService
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:22
 */
@Component
@SuppressWarnings("rawtypes")
public class JanitorEventService {
    private static final Logger log = LoggerFactory.getLogger(JanitorEventService.class);
    @Autowired
    private Environment env;
    @Autowired
    private EtcdDao etcdDao;
    @Resource
    private SnowflakeIdWorkerUtil snowflakeIdWorkerUtil;
    @Autowired
    private RegistryCacheService registryCacheService;
    private static final ConcurrentHashMap<String, EventLogWriter> LOG_WRITER_MAP = new ConcurrentHashMap<>();
    private volatile Map<String, Set> witheList = new HashMap<>();
    private volatile Map<String, Set> triggerMap = new HashMap<>();

    public JanitorEventService() {
    }

    @PostConstruct
    public void init() {
        List<RegistryBean> registryBeans = this.registryCacheService.getLocalCache();
        if (registryBeans != null) {
            this.registryCacheService.getLocalCache().forEach(this::registerEvent);
        }

        String whiteListJson = this.etcdDao.getEtcdServiceV3().get("janitor.whiteList");
        if ("".equals(whiteListJson)) {
            log.info("init  event whiteList is empty");
        } else {
            this.witheList = JsonUtil.parseMap(this.etcdDao.getEtcdServiceV3().get("janitor.whiteList"), String.class, Set.class);
        }

        this.etcdDao.getEtcdServiceV3().watch("janitor.whiteList", "janitor.whiteList", (info) -> {
                    EtcdEventKeyValueVo kvs = info.getCurrent();
                    log.info("事件白名单变更推送，动作类型{},推送内容为{}", info.getEventType(), kvs);
                    String value = kvs.getValue();
                    if ("".equals(value)) {
                        this.witheList = new ConcurrentHashMap<>();
                    } else {
                        this.witheList = JsonUtil.parseMap(value, String.class, Set.class);
                    }

                }
                , (error) -> log.error("监听出错", error)
                , (complete) -> log.info("监听完成{}", complete));
        String triggerMapJson = this.etcdDao.getEtcdServiceV3().get("janitor.triggerMap");
        if ("".equals(triggerMapJson)) {
            log.info("init  event triggerMap is empty");
        } else {
            this.triggerMap = JsonUtil.parseMap(this.etcdDao.getEtcdServiceV3().get("janitor.triggerMap"), String.class, Set.class);
        }

        this.etcdDao.getEtcdServiceV3().watch("janitor.triggerMap", "janitor.triggerMap", (info) -> {
                    EtcdEventKeyValueVo kvs = info.getCurrent();
                    log.info("事件关联关系变更推送，动作类型{},推送内容为{}", info.getEventType(), kvs);
                    String value = kvs.getValue();
                    if ("".equals(value)) {
                        this.triggerMap = new ConcurrentHashMap<>();
                    } else {
                        this.triggerMap = JsonUtil.parseMap(value, String.class, Set.class);
                    }

                }
                , (error) -> log.error("监听出错", error)
                , (complete) -> log.info("监听完成{}", complete)
        );
    }

    public void registerEvent(RegistryBean registryBean) {
        if (registryBean.getEvent()) {
            String key = EVENT_KEY_PREFIX + registryBean.getApp() + "." + this.registryCacheService.getLocalIp();
            this.etcdDao.getEtcdServiceV3().cancelWatch(key);
            this.etcdDao.getEtcdServiceV3().watch(key, key, (info) -> {
                        EtcdEventKeyValueVo kvs = info.getCurrent();
                        log.info("接收到事件相关推送，动作类型{},推送内容为{}", info.getEventType(), kvs);
                        if (EtcdEventVo.EventType.PUT.equals(info.getEventType()) && !kvs.getValue().contains("response")) {
                            String keyName = kvs.getKey();
                            int start = keyName.indexOf(".");
                            int end = keyName.indexOf(".", start + 1);
                            String appName = keyName.substring(start + 1, end);

                            try {
                                EventLogWriter eventLogWriter = LOG_WRITER_MAP.computeIfAbsent(appName, (app) -> {
                                    try {
                                        return new EventLogWriter(registryBean.getLocalPath(), registryBean.getApp(), 3, 16777216);
                                    } catch (FileNotFoundException e) {
                                        log.error("事件目录设置有误", e);
                                        return null;
                                    }
                                });
                                if (null == eventLogWriter) {
                                    return;
                                }

                                eventLogWriter.writeLine(keyName + "=" + this.encode(kvs.getValue()));
                            } catch (IOException e) {
                                log.error("写入事件到事件日志文件出错", e);
                            }

                        }
                    }
                    , (error) -> log.error("监听出错", error)
                    , (complete) -> log.info("监听完成{}", complete)
            );
        }

    }

    public boolean notify(EventResultNotifyReq req) {
        return this.etcdDao.getEtcdServiceV3().put(req.getKey(), JsonUtil.toJson(req));
    }

    public boolean heartBeat(HeartbeatDTO heartbeatDTO) {
        EtcdAppHeartbeatDTO reqObj = EtcdAppHeartbeatDTO.builder()
                .appName(heartbeatDTO.getApp())
                .ip(this.registryCacheService.getLocalIp())
                .beatTime(new Date())
                .build();
        boolean result = this.etcdDao.getEtcdServiceV3().put(HEARTBEAT_PREFIX + reqObj.getAppName() + "." + reqObj.getIp(), JsonUtil.toJson(reqObj));
        if (result) {
            log.debug("往ETCD服务器上报心跳正常,心跳内容{}", reqObj);
        } else {
            log.error("往ETCD服务器上报心跳发送失败,心跳内容{}", reqObj);
        }

        return result;
    }

    private String encode(Object str) {
        return null != str && !"".equals(str) ? new String(Base64.getEncoder().encode(str.toString().getBytes()), StandardCharsets.UTF_8) : "";
    }

    @SuppressWarnings("unchecked")
    public EventPushResult pushEvent(EventPushReq req) {
        EventTypeEnums eventTypeEnums = EventTypeEnums.valueOf(req.getEventType());
        Map<String, Object> pushList = new HashMap<>();
        switch (eventTypeEnums) {
            case CACHE_EXPIRE:
                Set withList = this.witheList.get(eventTypeEnums.name());
                if (null != withList && !withList.isEmpty()) {
                    if (!withList.contains(req.getAppName())) {
                        return EventPushResult.error("the app not in the  whiteList");
                    }

                    if (this.triggerMap.containsKey(req.getAppName()) && !this.triggerMap.get(req.getAppName()).isEmpty()) {
                        Set<String> appList = this.triggerMap.get(req.getAppName());
                        long currentTime = (new Date()).getTime();
                        List<EtcdOperation> etcdOperationList = appList.stream()
                                .filter((appName) -> null == req.getTargetAppNames() || req.getTargetAppNames().isEmpty() || req.getTargetAppNames().contains(appName))
                                .map((appName) -> {
                                    String searchKey = HEARTBEAT_PREFIX + appName + ".";
                                    Map<String, String> heartBeatInfo = this.etcdDao.getEtcdServiceV3().getPrefix(searchKey);
                                    Set<String> ipList = new HashSet<>();
                                    List<EtcdOperation> opList = new ArrayList<>();
                                    heartBeatInfo.forEach((key, value) -> {
                                        EtcdAppHeartbeatDTO heartbeatDTO = JsonUtil.parse(value, EtcdAppHeartbeatDTO.class);
                                        if (null != heartbeatDTO.getBeatTime()) {
                                            long beatTime = heartbeatDTO.getBeatTime().getTime();
                                            if (currentTime - beatTime <= 10000L) {
                                                String[] split = key.split("\\.", 3);
                                                String ip = split[2];
                                                ipList.add(ip);
                                                String etcdKey = EVENT_KEY_PREFIX.concat(appName).concat(".").concat(ip).concat(".").concat(String.valueOf(this.snowflakeIdWorkerUtil.nextId()));
                                                EtcdOperation etcdOperation = EtcdOperation.builder()
                                                        .key(etcdKey)
                                                        .operateType(EtcdOperation.OperateType.PUT)
                                                        .value(String.format(EVENT_KEY_VALUE_FORMAT, req.getEventType(), req.getEventValue()))
                                                        .build();
                                                opList.add(etcdOperation);
                                            }
                                        }
                                    });
                                    pushList.put(appName, ipList);
                                    return opList;
                                }).flatMap(Collection::stream).collect(Collectors.toList());
                        if (etcdOperationList.isEmpty()) {
                            return EventPushResult.error("empty push server info");
                        }

                        boolean pushResult = this.etcdDao.getEtcdServiceV3().batchOperate(etcdOperationList);
                        return pushResult ? EventPushResult.success(pushList) : EventPushResult.error("etcd push event error");
                    }

                    return EventPushResult.error("event push AppList is empty");
                }

                return EventPushResult.error("event witheList is empty");
            case GRAY_PUBLISH:
            default:
                return EventPushResult.error("not support event type");
        }
    }
}
