package com.black.core.spring.factory;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public interface AgentObject {

    /**
     * 返回真实代理的对象
     * @return 对象
     */
    Object getProxyObject();

    /**
     * 返回代理的对象的class对象
     * @return class
     */
    Class<?> getAgentClazz();

    //enhance getAgentClazz
    default ClassWrapper<?> getAgentClazzWrapper(){
        return ClassWrapper.get(getAgentClazz());
    }

    /***
     * 调用传递的参数
     * @return 参数列表
     */
    Object[] getArgs();

    /**
     * 是否为最后一个处理器
     * @return 返回布尔值
     */
    boolean isLastLayer();

    /**
     * 是否为jdk代理
     * @return
     */
    boolean isJDK();

    /**
     * 是否为 cglib代理
     * @return
     */
    boolean isCGLIB();

    /**
     * 如果是 cglib 代理则不会返回空, 否则返回空
     * @return
     */
    MethodProxy getMethodProxy();

    //enhance getProxyMethod
    default MethodWrapper getProxyMethodWrapper(){
        return MethodWrapper.get(getProxyMethod());
    }

    /***
     * 获取代理的方法
     * @return 方法对象
     */
    Method getProxyMethod();

    /***
     * 向下执行
     * @param args 执行参数
     * @return 返回下面执行的结果
     */
    Object doFlow(Object[] args) throws Throwable;

    /**
     * 获取上一层代理对象
     * @return {@link AgentLayer}
     */
    AgentLayer getUpperStoryAgentLayer();

    /**
     * 获取下一层代理对象
     * @return {@link AgentLayer}
     */
    AgentLayer getNextFloorAgentLayer();

    /***
     * 获取代理层数
     * @return 数量
     */
    int getNumberOfAgentLayers();

    /**
     * 获取所有的代理,并返回一个链表
     * @return 返回链表
     */
    ProxyLayerQueue getAgentQueue();

    /**
     * 重置
     * @param args 新的参数
     */
    void clear(Object[] args);
}
