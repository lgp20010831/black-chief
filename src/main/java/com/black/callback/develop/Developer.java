package com.black.callback.develop;

/**
 * 开发者
 * @author 李桂鹏
 * @create 2023-05-17 13:36
 */
@SuppressWarnings("all")
public interface Developer {

    default boolean attachThrowable(){
        return false;
    }

    /**
     * 当开发上下文未初始化时
     * 也是当前实例 实例化结束时执行
     */
    default void prepare(){

    }

    /**
     * 当组件被拒绝加入上下文中时执行
     * @param context 上下文
     * @param decisionMaker 拒绝你的决策者
     */
    default void rejected(DevelopmentContext context, Developer decisionMaker){

    }

    /**
     * 当你成功被注册到上下文中时执行
     * @param context 上下文
     */
    default void registered(DevelopmentContext context){

    }

    default void applicationPrepareLoad(DevelopmentContext context){


    }

    default void applicationStarted(DevelopmentContext context){

    }

    default void applicationRunning(DevelopmentContext context){

    }

    default void applicationFailed(DevelopmentContext context){

    }

    default void shutdown(DevelopmentContext context){

    }
}
