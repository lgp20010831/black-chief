package com.black.nio.group;

public interface GioMapper extends Gio{

    String getName();

    GioContext getContext();

    default void writeAndFlush(Object source){
        getContext().writeAndFlush(source);
    }

    default void write(Object source){
        getContext().write(source);
    }

    default void flush(){
        getContext().flush();
    }

    default void shutdown(){
        getContext().shutdown();
    }


}
