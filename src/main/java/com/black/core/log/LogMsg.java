package com.black.core.log;

public class LogMsg {

    private final LogLevel logLevel;

    private String msg;

    public LogMsg(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
