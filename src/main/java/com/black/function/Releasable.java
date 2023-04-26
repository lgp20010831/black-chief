package com.black.function;

public interface Releasable {


    //当容器开启当前对象
    default void open(){

    }

    //当外部操控着释放当前对象时调用
    default void release(){

    }

    //当对象被容器所丢弃的时候,则会触发
    default void discard(){

    }
}
