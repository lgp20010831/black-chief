package com.black.core.log;

import java.io.IOException;
import java.io.InputStream;

public interface Catalog {

    default int state(){
        return -1;
    }

    default void setState(int state){
        throw new IllegalStateException("not support state");
    }

    void info(String msg);

    void debug(String msg);

    void trace(String msg);

    void error(String msg);

    void flush();

    default InputStream getInputStream() throws IOException {
        throw new IOException("can not open inputstream");
    }

    default String stringStack(){
        throw new UnsupportedOperationException("no stack wried string");
    }

    void close();

    boolean isEnabledInfo();

    boolean isEnabledDebug();

    boolean isEnabledTrace();

    boolean isEnabledError();

}
