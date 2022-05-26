package com.janitor.client.thread;

import com.janitor.client.listener.AbstractEventListener;
import com.janitor.common.http.HttpCallback;
import com.janitor.common.http.HttpUtil;
import com.janitor.common.http.R;
import com.janitor.common.json.JsonUtil;
import com.janitor.common.model.EventErrorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * ClassName EventMonitor
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:17
 */
public class EventMonitor extends Thread {
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(EventMonitor.class);

    /**
     * 空事件睡眠时间间隔
     */
    private final long emptyEventInterval;
    /**
     * 文件夹
     */
    private final String dir;
    /**
     * 事件文件的索引文件，存储文件已经读取的位置
     */
    private File positionFile;
    /**
     * 事件文件的索引文件BufferedWriter
     */
    private BufferedWriter positionWriter;
    /**
     * 是否轮训标志
     */
    private boolean tailing;
    /**
     * 时间监听者
     */
    private final Set<AbstractEventListener> listeners;
    /**
     * httpUtil
     */
    HttpUtil httpUtil;
    /**
     * 应用程序名称
     */
    private final String appName;
    /**
     * janitor-server的url
     */
    private String janitorServerUrl;

    public EventMonitor(String dir, String appName, long emptyEventInterval) {
        this.tailing = false;
        this.listeners = new HashSet<>();
        this.httpUtil = (new HttpUtil()).connections(4).start();
        this.dir = dir;
        this.appName = appName;
        this.emptyEventInterval = emptyEventInterval;
    }

    public EventMonitor(String dir, String appName) {
        this(dir, appName, 1000L);
    }

    public void addEventListener(AbstractEventListener listener) {
        this.listeners.add(listener);
    }

    public boolean getHasEventListener() {
        return this.listeners != null && this.listeners.size() > 0;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        if (logger.isInfoEnabled()) {
            logger.info("event monitor run start listeners {}", this.listeners);
        }

        long filePointer = 0L;
        int fileIdx = 0;
        this.positionFile = new File(this.dir + File.separator + this.appName + File.separator + "position");
        if (this.positionFile.exists()) {
            BufferedReader bufferedReader = null;

            try {
                FileReader reader = new FileReader(this.positionFile);
                bufferedReader = new BufferedReader(reader);
                if (this.positionFile.length() != 0L) {
                    String positionTxt = bufferedReader.readLine();
                    if (positionTxt != null && !positionTxt.trim().isEmpty() && positionTxt.matches("\\d+,\\d+")) {
                        String[] items = positionTxt.split(",");
                        fileIdx = Integer.parseInt(items[0]);
                        filePointer = Integer.parseInt(items[1]);
                    }
                }
            } catch (FileNotFoundException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("事件索引文件不存在", e);
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("事件索引文件读取失败", e);
                }
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        try {
            while (this.tailing) {
                File eventDir = new File(this.dir + File.separator + this.appName);
                if (!eventDir.isDirectory()) {
                    sleep(this.emptyEventInterval);
                } else {
                    String[] files = eventDir.list((dir, name) -> name.startsWith("event.log."));
                    if (files != null && files.length != 0) {
                        File logfile = new File(this.dir + File.separator + this.appName + File.separator + "event.log." + fileIdx);
                        if (!logfile.exists()) {
                            ++fileIdx;
                        } else {
                            RandomAccessFile file = new RandomAccessFile(logfile, "r");
                            long fileLength = logfile.length();
                            if (fileLength <= filePointer) {
                                ++fileIdx;
                                logfile = new File(this.dir + File.separator + this.appName + File.separator + "event.log." + fileIdx);
                                if (!logfile.exists()) {
                                    --fileIdx;
                                    file.close();
                                    sleep(this.emptyEventInterval);
                                    continue;
                                }

                                file.close();
                                file = new RandomAccessFile(logfile, "r");
                                filePointer = 0L;
                            }

                            if (fileLength > filePointer) {
                                file.seek(filePointer);
                                String line = file.readLine();

                                while (line != null && !line.isEmpty()) {
                                    String[] kvArray = line.split("=", 2);
                                    String key = kvArray[0];
                                    if (key.startsWith("event." + this.appName)) {
                                        if (logger.isInfoEnabled()) {
                                            logger.info("receive event :{}", line);
                                        }

                                        Map<String, Object> eventEntity = JsonUtil.parseMap(new String(Base64.getDecoder().decode(kvArray[1].getBytes(StandardCharsets.UTF_8))), String.class, Object.class);
                                        String eventType = eventEntity.get("eventType").toString();
                                        String eventValue = eventEntity.get("eventValue").toString();
                                        List<EventErrorEntity> errorList = new CopyOnWriteArrayList<>();
                                        String finalLine = line;
                                        this.listeners.stream().filter((listener) -> {
                                            if (logger.isInfoEnabled()) {
                                                logger.info("listener event type:{} receive event type:{} result{}", listener.event().name(), eventType, listener.event().name().equals(eventType));
                                            }

                                            return listener.event().name().equals(eventType);
                                        }).forEach((listener) -> {
                                            int retryTime = 0;

                                            while (retryTime < listener.getRetries()) {
                                                if (logger.isInfoEnabled()) {
                                                    logger.info("before exec event :{}", eventValue);
                                                }

                                                try {
                                                    listener.exec(eventValue);
                                                    break;
                                                } catch (Exception e) {
                                                    if (retryTime == 0) {
                                                        logger.warn("event process error, event content is [{}]", finalLine);
                                                    }

                                                    if (retryTime == listener.getRetries() - 1) {
                                                        logger.warn("event listener process error and retry {} times, will ignore this event and read next event!", listener.getRetries());
                                                        errorList.add(new EventErrorEntity(listener.getClass().getName(), e.getMessage()));
                                                    } else {
                                                        Double retryIntervalNow = listener.isMultipleInterval() ? (double) listener.getRetryInterval() * Math.pow(2.0D, retryTime) : (double) listener.getRetryInterval();
                                                        logger.warn("event listener process error, error info is {}, retry times is {}, retry interval is {}", e.getMessage(), retryTime, retryIntervalNow);

                                                        try {
                                                            TimeUnit.MILLISECONDS.sleep(retryIntervalNow.longValue());
                                                        } catch (InterruptedException interruptedException) {
                                                            interruptedException.printStackTrace();
                                                        }
                                                    }

                                                    ++retryTime;
                                                }
                                            }

                                        });
                                        Map<String, Object> responseMap = new HashMap<>();
                                        responseMap.put("code", errorList.size() > 0 ? 1 : 0);
                                        responseMap.put("errors", errorList);
                                        eventEntity.put("response", responseMap);
                                        eventEntity.put("key", kvArray[0]);
                                        String json = JsonUtil.toJson(eventEntity);
                                        this.httpUtil.asyncPost(this.janitorServerUrl + "/api/notify", null, null, json, new HttpCallback() {
                                            @Override
                                            public void completed(R r) {
                                                if (EventMonitor.logger.isInfoEnabled()) {
                                                    EventMonitor.logger.info("config event confirm result:{}", r.getResponseText());
                                                }

                                            }

                                            @Override
                                            public void failed(Exception ex) {
                                                EventMonitor.logger.error("config event confirm error:", ex);
                                            }
                                        }, 3000);
                                    }
                                    line = file.readLine();
                                }

                                filePointer = file.getFilePointer();
                                this.writePosition(fileIdx, filePointer);
                                file.close();
                            }

                            sleep(this.emptyEventInterval);
                        }
                    } else {
                        sleep(this.emptyEventInterval);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            logger.error("config event Monitor error", e);
        }

    }

    private void writePosition(int fileIdx, long filePointer) {
        try {
            this.positionWriter = new BufferedWriter(new FileWriter(this.positionFile, false));
            this.positionWriter.write(fileIdx + "," + filePointer);
            this.positionWriter.flush();
        } catch (IOException e) {
            logger.error("config event Monitor update position error", e);
        } finally {
            try {
                this.positionWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void setTailing(boolean tailing) {
        this.tailing = tailing;
    }

    public void setJanitorServerUrl(String janitorServerUrl) {
        this.janitorServerUrl = janitorServerUrl;
    }
}
