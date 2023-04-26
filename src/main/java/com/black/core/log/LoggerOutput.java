package com.black.core.log;

public interface LoggerOutput {


    void println(String msg, LogLevel level);

    void flush();
}
