package com.black.nio.group;

public interface GioContext extends Gio{

    Object source();

    NioType getType();

    ContextType getContextType();

    Configuration getConfiguration();

    void executeWork(Runnable runnable);

}
