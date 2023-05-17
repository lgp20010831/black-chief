package com.black.callback.develop;

import java.util.Collection;
import java.util.List;

/**
 * 想给各个组件提供在 spring 环境下穿插执行的功能
 * @author 李桂鹏
 * @create 2023-05-17 13:39
 */
@SuppressWarnings("all")
public interface DevelopmentContext {

    void registerDeveloper(Developer developer);

    <T> List<T> getParticularDeveloper(Class<T> type);

    void postSources(Collection<Class<?>> source);

    void prepareLoad();

    void failed();

    void running();

    void started();

    void shutdown();
}
