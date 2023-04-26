package com.black.core.spring.factory;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.spring.instance.InstanceFactory;

import java.util.Collection;

public interface ReusingProxyFactory extends Factory {
    /**
     * 检查该 class 对象是否被代理
     * @param proxiedObject class
     * @return true: 被代理过, false: 否
     */
    @SuppressWarnings("all")
    boolean isAgent(Class<?> proxiedObject);

    <T> T getAlreadyProxiedObject(Class<T> type);

    /** 默认指定代理的下标 */
    default <T> T proxy(Class<? extends T> proxiedObject, AgentLayer agentLayer){
        return proxy(proxiedObject, agentLayer, -1);
    }

    default <T> T proxy(Class<? extends T> proxiedObject, Class<?>[] paramters, Object[] args, AgentLayer agentLayer){
        return proxy(proxiedObject, paramters, args, agentLayer, -1);
    }

    default <T> T proxy(Class<? extends T> proxiedObject, AgentLayer agentLayer, int index){
        return proxy(proxiedObject, null, null, agentLayer, index);
    }
    /**
     * 代理该 class 对象, 并指定代理的位置
     * @param proxiedObject 目标 class
     * @param agentLayer 逻辑处理器
     * @param index 代理的下标, 如果不填默认加到队列尾部
     * @param <T> 被代理对象类型
     * @return 返回代理后的对象
     */
    <T> T proxy(Class<? extends T> proxiedObject, Class<?>[] paramters, Object[] args, AgentLayer agentLayer, int index);

    <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, InstanceFactory instanceFactory, int index);

    <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, InstanceFactory instanceFactory);

    <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, BeanFactory beanFactory, int index);

    default <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, BeanFactory beanFactory){
        return proxy(proxiedObject, layer, beanFactory, -1);
    }

    default <T> T prototypeProxy(Class<? extends T> proxiedObject, AgentLayer layer){
        return prototypeProxy(proxiedObject, layer, null, null);
    }

    <T> T prototypeProxy(Class<? extends T> proxiedObject, AgentLayer layer, Class<?>[] paramters, Object[] args);

    /***
     * 获取代理层数
     * @param proxiedObject 要查询的目标,如果该对象没有被代理, 返回 -1
     * @return 数量
     */
    int getNumberOfAgentLayers(Class<?> proxiedObject);

    /**
     * 获取所有的代理,并返回一个链表
     * @param proxiedObject 要查询的目标,如果该对象没有被代理, 返回空
     * @return 返回链表
     */
    ProxyLayerQueue getAgentQueue(Class<?> proxiedObject);

    /***
     * 获取代理的所有对象的实例
     * @return 返回实例对象集合
     */
    Collection<Object> getProxyInstanceCache();
}
