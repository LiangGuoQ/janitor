package com.janitor.client;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.janitor.client.listener.AbstractEventListener;
import com.janitor.client.processor.ConfigProcessor;
import com.janitor.client.thread.EventMonitor;
import com.janitor.common.enums.EventTypeEnums;
import com.janitor.common.http.HttpCallback;
import com.janitor.common.http.HttpUtil;
import com.janitor.common.http.R;
import com.janitor.common.json.JsonUtil;
import com.janitor.common.model.EventPushReq;
import com.janitor.common.model.EventPushResult;
import com.janitor.common.model.HeartbeatDTO;
import com.janitor.common.model.RegistryBean;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName RegistryService
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:03
 */
public class RegistryService extends FileAlterationListenerAdaptor {

    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);

    /**
     * 配置文件监控
     */
    FileAlterationMonitor monitor;
    /**
     * 旧配置信息
     */
    private final ConcurrentHashMap<String, String> oldProp = new ConcurrentHashMap<>();
    /**
     * 本地路径
     */
    private final String localPath;
    /**
     * 应用程序名称
     */
    private final String app;
    /**
     * janitorServer主机端口
     */
    private String janitorServerHostPort = "6237";
    /**
     * janitorServer服务器ip
     */
    private String janitorServerIp = "127.0.0.1";
    /**
     * 配置信息的前缀key数组
     */
    private final String[] configData;
    /**
     * 事件监视器
     */
    private final EventMonitor eventMonitor;
    /**
     * ip正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-1]\\d|22[0-3])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    /**
     * 配置文件名字
     */
    private static final String PROPERTIES_FILE_NAME = "app.properties";
    /**
     * 周期线程池
     */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    /**
     * 配置回调处理
     */
    private static final List<ConfigProcessor> CALLBACKS = new ArrayList<>();
    /**
     * httpUtil
     */
    HttpUtil httpUtil = (new HttpUtil()).connections(10).setSocketTimeout(3000).setConnectTimeout(1000).setConnectRequestTimeout(500).start();

    public RegistryService(String localPath, String app, String... configs) {
        this.localPath = localPath;
        this.app = app;
        this.configData = configs;
        this.monitor = new FileAlterationMonitor(500L);
        FileAlterationObserver observer = new FileAlterationObserver(localPath + "/" + app);
        observer.addListener(this);
        this.monitor.addObserver(observer);
        this.eventMonitor = new EventMonitor(localPath, app);
    }

    private Properties readPropInFile(String filePath) {
        Properties prop = new Properties();
        File file = new File(filePath);
        if (file.exists()) {
            try {
                FileInputStream readFileInputStream = new FileInputStream(file);
                Throwable throwable = null;
                try {
                    prop.load(readFileInputStream);
                } catch (Throwable loadThrowable) {
                    throwable = loadThrowable;
                    throw loadThrowable;
                } finally {
                    if (throwable != null) {
                        try {
                            readFileInputStream.close();
                        } catch (Throwable closeThrowable) {
                            throwable.addSuppressed(closeThrowable);
                        }
                    } else {
                        readFileInputStream.close();
                    }
                }
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("启动读取配置文件失败", e);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("缓存中当前配置信息：{}", prop);
            }

        }
        return prop;
    }

    private Properties diffDelete(Properties newProp) {
        Properties properties = new Properties();
        this.oldProp.forEach((key, value) -> {
            if (!newProp.containsKey(key)) {
                properties.put(key, this.decode(value));
            }

        });
        return properties;
    }

    private Properties diffUpdate(Properties newProp) {
        Properties properties = new Properties();
        newProp.forEach((key, value) -> {
            if (this.oldProp.containsKey(key.toString()) && !this.oldProp.get(key.toString()).equals(value)) {
                properties.put(key, this.decode(value));
            }

        });
        return properties;
    }

    private Properties diffAdd(Properties newProp) {
        Properties properties = new Properties();
        newProp.entrySet().stream()
                .filter((entry) -> !this.oldProp.containsKey(entry.getKey().toString()))
                .forEach((entry) -> properties.put(entry.getKey(), this.decode(entry.getValue())));
        return properties;
    }

    public RegistryService setJanitorServerHostPort(String janitorServerHostPort) {
        this.janitorServerHostPort = janitorServerHostPort;
        return this;
    }

    public RegistryService setJanitorServerIp(String janitorServerIp) {
        Matcher matcher = IP_PATTERN.matcher(janitorServerIp);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("agentIp string is illegal");
        } else {
            this.janitorServerIp = janitorServerIp;
            return this;
        }
    }

    public RegistryService addProcessor(ConfigProcessor cb) {
        CALLBACKS.add(cb);
        return this;
    }

    public void start() {
        String janitorServerUrl = "http://" + this.janitorServerIp + ":" + this.janitorServerHostPort;
        if (this.configData.length > 0) {
            RegistryBean registryBean = RegistryBean.builder()
                    .app(this.app)
                    .localPath(this.localPath)
                    .event(this.eventMonitor.getHasEventListener())
                    .data(ListUtil.toList(this.configData))
                    .build();
            this.httpUtil.asyncPost(janitorServerUrl + "/api/registry", null, null, registryBean, new HttpCallback() {
                @Override
                public void completed(R r) {
                    if (RegistryService.LOGGER.isInfoEnabled()) {
                        RegistryService.LOGGER.info("注册配置结果{}", r.getResponseText());
                        RegistryService.LOGGER.info("开始读取本地缓存配置");
                    }

                    Properties properties = RegistryService.this.readPropInFile(RegistryService.this.localPath + "/" + RegistryService.this.app + "/app.properties");
                    RegistryService.CALLBACKS.forEach((e) -> {
                        Properties matchedProps = RegistryService.this.decode(e.match(properties));
                        if (!matchedProps.isEmpty()) {
                            e.onLoad(matchedProps);
                            e.onAdd(matchedProps);
                        }

                    });
                    RegistryService.this.replaceOldPropElements(properties);
                }

                @Override
                public void failed(Exception ex) {
                    RegistryService.LOGGER.error("注册配置服务出错{}", ex.getMessage());
                }
            }, 3000);
        }

        try {
            this.monitor.start();
        } catch (Exception e) {
            LOGGER.error("event monitor start failed ", e);
        }

        this.eventMonitor.setJanitorServerUrl(janitorServerUrl);
        this.eventMonitor.setTailing(true);
        this.eventMonitor.start();
        this.heartbeat(janitorServerUrl);
    }

    private void heartbeat(String janitorServerUrl) {
        this.executorService.scheduleAtFixedRate(() -> {
            HeartbeatDTO dto = HeartbeatDTO.builder()
                    .app(this.app)
                    .build();
            this.httpUtil.asyncPost(janitorServerUrl + "/api/heartbeat", null, null, dto, new HttpCallback() {
                @Override
                public void completed(R r) {
                    if (RegistryService.LOGGER.isDebugEnabled()) {
                        RegistryService.LOGGER.debug("心跳发送成功，相应结果：{}", r.getResponseText());
                    }

                }

                @Override
                public void failed(Exception ex) {
                    RegistryService.LOGGER.error("心跳发送失败，请检查agent服务是否正常:{}", ex.getMessage());
                }
            }, 3000);
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    public String getProp(String key) {
        return this.decode(this.oldProp.get(key));
    }

    public EventPushResult send(EventTypeEnums eventType, String eventValue, String... targetAppNames) {
        if (null == eventType) {
            return EventPushResult.error("事件类型不能为空");
        } else {
            String janitorServerUrl = "http://" + this.janitorServerIp + ":" + this.janitorServerHostPort;
            EventPushReq pushReq = EventPushReq.builder()
                    .appName(this.app)
                    .eventType(eventType.name())
                    .eventValue(eventValue)
                    .targetAppNames(ListUtil.toList(targetAppNames))
                    .build();
            R postResult = this.httpUtil.post(janitorServerUrl + "/api/event", null, null, pushReq, 3000);
            String responseText = postResult.getResponseText();
            LOGGER.info("发送结果：{}", responseText);
            return null != responseText && 200 == postResult.getStatusCode() ? JsonUtil.parse(responseText, EventPushResult.class) : EventPushResult.error("事件发送失败，请检查agent服务是否正常 ");
        }
    }

    private Properties decode(Properties properties) {
        Properties p = new Properties();
        properties.stringPropertyNames().forEach((key) -> p.put(key, this.decode(properties.getProperty(key))));
        return p;
    }

    @Override
    public void onFileChange(File file) {
        LOGGER.debug("修改文件【{}】", file.getName());
        if (StrUtil.equals(PROPERTIES_FILE_NAME, file.getName())) {
            this.onConfigChange(file);
        }

    }

    private void onConfigChange(File file) {
        Properties properties = new Properties();

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Throwable throwable = null;
            try {
                properties.load(fileInputStream);
            } catch (Throwable loadThrowable) {
                throwable = loadThrowable;
                throw loadThrowable;
            } finally {
                if (throwable != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable closeThrowable) {
                        throwable.addSuppressed(closeThrowable);
                    }
                } else {
                    fileInputStream.close();
                }

            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("获取配置文件内容出错", e);
            }
        }

        Properties diffDelete = this.diffDelete(properties);
        Properties diffUpdate = this.diffUpdate(properties);
        Properties diffAdd = this.diffAdd(properties);
        this.replaceOldPropElements(properties);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("检测到配置变更");
            LOGGER.info("新增：{}", diffAdd);
            LOGGER.info("更新：{}", diffUpdate);
            LOGGER.info("删除：{}", diffDelete);
        }

        CALLBACKS.forEach((e) -> {
            Properties matchedDiffAdd;
            if (!diffDelete.isEmpty()) {
                matchedDiffAdd = e.match(diffDelete);
                if (matchedDiffAdd != null && !matchedDiffAdd.isEmpty()) {
                    e.onDelete(matchedDiffAdd);
                }
            }

            if (!diffUpdate.isEmpty()) {
                matchedDiffAdd = e.match(diffUpdate);
                if (matchedDiffAdd != null && !matchedDiffAdd.isEmpty()) {
                    e.onUpdate(matchedDiffAdd);
                }
            }

            if (!diffAdd.isEmpty()) {
                matchedDiffAdd = e.match(diffAdd);
                if (matchedDiffAdd != null && !matchedDiffAdd.isEmpty()) {
                    e.onAdd(matchedDiffAdd);
                }
            }

        });
    }

    private String decode(Object str) {
        return null == str ? "" : new String(Base64.getDecoder().decode(str.toString()), StandardCharsets.UTF_8);
    }

    public RegistryService addEventListener(AbstractEventListener eventListener) {
        this.eventMonitor.addEventListener(eventListener);
        return this;
    }

    private void replaceOldPropElements(Properties properties) {
        this.oldProp.clear();
        properties.stringPropertyNames().forEach((key) -> {
            String value = this.oldProp.put(key, properties.getProperty(key));
        });
    }
}
