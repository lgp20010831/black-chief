package com.black.core.aop.servlet.flow;

public class FlowLocal {

    private static ThreadLocal<FlowMatedata> matedataThreadLocal = new ThreadLocal<>();

    public static FlowMatedata getMatadata(){
        return matedataThreadLocal.get();
    }

    public static void set(FlowMatedata matedata){
        matedataThreadLocal.set(matedata);
    }

    public static void remove(){
        matedataThreadLocal.remove();
    }
}
