package com.janitor.admin.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.janitor.admin.service.AppService;
import com.janitor.common.etcd.dao.EtcdDao;
import com.janitor.common.json.JsonUtil;
import com.janitor.common.model.EtcdAppHeartbeatDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.janitor.common.constant.EventConstants.HEARTBEAT_PREFIX;

/**
 * ClassName AppServiceImpl
 * Description
 *
 * @author 曦逆
 * Date 2022/5/26 13:42
 */
@Slf4j
@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private EtcdDao etcdDao;

    @Override
    public List<String> getAppNameList() {
        try {
            return etcdDao.getEtcdServiceV3().getPrefix(HEARTBEAT_PREFIX)
                    .keySet()
                    .stream()
                    .map(s -> {
                        String[] split = s.split("\\.", 3);
                        return split[1];
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取应用列表出错", e);
            return ListUtil.empty();
        }
    }

    @Override
    public List<String> getIpList(String appName) {
        String searchKey = HEARTBEAT_PREFIX + appName + ".";
        long currentTime = System.currentTimeMillis();
        List<String> list = null;
        try {
            return etcdDao.getEtcdServiceV3()
                    .getPrefix(searchKey)
                    .entrySet()
                    .stream()
                    .filter((entry) -> {
                        EtcdAppHeartbeatDTO heartbeatDTO = JsonUtil.parse(entry.getValue(), EtcdAppHeartbeatDTO.class);
                        if (null == heartbeatDTO.getBeatTime()) {
                            return false;
                        } else {
                            long beatTime = heartbeatDTO.getBeatTime().getTime();
                            return currentTime - beatTime < 10000L;
                        }
                    })
                    .map((entry) -> {
                        String[] split = entry.getKey().split("\\.", 3);
                        return split[2];
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取应用ip列表出错", e);
            return ListUtil.empty();
        }
    }
}
