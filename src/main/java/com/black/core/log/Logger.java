package com.black.core.log;

public interface Logger {

    void info(String txt, Object... params);

    void error(String txt, Object... params);

    void debug(String txt, Object params);

    boolean isInfoEnabled();

    boolean isErrorEnabled();

    boolean isDebugEnabled();
}
