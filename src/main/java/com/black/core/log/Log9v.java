package com.black.core.log;

public interface Log9v extends Logger{

    String getName();

    void print(String txt, int lazyTimeOut, Object... params);

}
