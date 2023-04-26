package com.black.rpc.log;

public interface Log {

    void info(String txt, Object... params);

    void debug(String txt, Object... params);

    void error(String txt, Object... params);

    void error(Throwable e);
}
