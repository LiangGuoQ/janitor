package com.janitor.common.util;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName EventLogWriter
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:10
 */
@Data
public class EventLogWriter {
    private static final Logger logger = LoggerFactory.getLogger(EventLogWriter.class);
    private int idx = 0;
    private String dir;
    private String prefix = "event.log";
    private String appName;
    private static final String FILE_TEMPLATE = "%s/%s/%s.%d";
    private int maxSize;
    private int maxHistory;
    private File logFile;
    private BufferedWriter writer;
    private FileOutputStream fos;

    public EventLogWriter(String dir, String appName, int maxHistory, int maxSize) throws FileNotFoundException {
        this.dir = dir;
        this.appName = appName;
        this.maxHistory = maxHistory;
        this.maxSize = maxSize;
        this.createWriter();
    }

    public void writeLine(String log) throws IOException {
        long fileSize = this.logFile.length();
        if (fileSize >= (long) this.maxSize) {
            ++this.idx;
            this.writer.flush();
            this.writer.close();
            this.logFile = new File(getLogFileName());
            this.fos = new FileOutputStream(this.logFile, true);
            this.writer = new BufferedWriter(new OutputStreamWriter(this.fos));
            fileSize = this.logFile.length();
            this.removeHistory();
        }

        if (fileSize > 0L) {
            this.writer.newLine();
        }

        this.writer.write(log);
        this.writer.flush();
    }

    private void removeHistory() {
        File fileDir = new File(this.dir + "/" + this.appName);
        if (fileDir.exists()) {
            String[] eventFiles = fileDir.list((dir, name) -> name.matches(this.prefix + "\\.\\d+"));
            if (eventFiles != null && eventFiles.length >= this.maxHistory) {
                List<Integer> fileIdx = new ArrayList<>(eventFiles.length);
                int eventFilesLen = eventFiles.length;

                for (int i = 0; i < eventFilesLen; ++i) {
                    String eventFile = eventFiles[i];
                    String idxStr = eventFile.substring(eventFile.lastIndexOf(".") + 1);
                    int idxNum = Integer.parseInt(idxStr);
                    fileIdx.add(idxNum);
                }

                fileIdx.sort(Comparator.comparingInt((o) -> o));
                int removeNeedCount = eventFiles.length - this.maxHistory;

                for (eventFilesLen = 0; eventFilesLen < removeNeedCount; ++eventFilesLen) {
                    File needDeleteFile = new File(String.format("%s/%s/%s.%d", this.dir, this.appName, this.prefix, fileIdx.get(eventFilesLen)));
                    if (needDeleteFile.exists()) {
                        boolean deleteResult = needDeleteFile.delete();
                        if (!deleteResult) {
                            logger.error("删除历史事件日志失败");
                        }
                    }
                }

            }
        }
    }

    private void createWriter() throws FileNotFoundException {
        File fileDir = new File(this.dir + "/" + this.appName);
        if (fileDir.exists()) {
            String[] eventFiles = fileDir.list((dir, name) -> name.matches(this.prefix + "\\.\\d+"));
            if (eventFiles != null) {
                int eventFilesLen = eventFiles.length;
                for (String eventFile : eventFiles) {
                    String idxStr = eventFile.substring(eventFile.lastIndexOf(".") + 1);
                    int idxNum = Integer.parseInt(idxStr);
                    if (idxNum > this.idx) {
                        this.idx = idxNum;
                    }
                }
            }
        } else {
            boolean mkdirResult = fileDir.mkdirs();
            if (!mkdirResult) {
                logger.error("创建事件文件目录失败");
            }
        }

        this.logFile = new File(getLogFileName());
        this.fos = new FileOutputStream(this.logFile, true);
        this.writer = new BufferedWriter(new OutputStreamWriter(this.fos));
    }

    private String getLogFileName() {
        return String.format("%s/%s/%s.%d", this.dir, this.appName, this.prefix, this.idx);
    }
}

