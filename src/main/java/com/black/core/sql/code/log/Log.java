package com.black.core.sql.code.log;

public interface Log {

    boolean isDebugEnabled();

    boolean isErrorEnabled();

    boolean isInfoEnabled();

    void info(String str);

    void debug(String str);

    void error(String str);

    default void flush(){}
}
