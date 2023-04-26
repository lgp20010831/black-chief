package com.black.core.log;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LogFactory {

    private static File loggerFile;

    private static boolean delay = false;

    public static void absolutelyLoggerFile(String path){
        File file = new File(path);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                file = null;
            }
        }
        setLoggerFile(file);
    }

    public static void configLoggerFile(String path){
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null){
            loggerFile = new File(resource.getFile());
        }
    }

    public static void setDelay(boolean delay) {
        LogFactory.delay = delay;
    }

    public static void setLoggerFile(File loggerFile) {
        LogFactory.loggerFile = loggerFile;
    }

    public static IoLog getArrayLog(){
        ArrayIoLog ioLog = loggerFile == null ? new ArrayIoLog() : new ArrayIoLog(loggerFile);
        ioLog.enabledDelay(delay);
        return ioLog;
    }

    public static IoLog getLog4j(){
        return new CommonLog4jLog();
    }

}
