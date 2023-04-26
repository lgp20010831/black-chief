package com.black.core.factory;

import com.black.core.spring.instance.InstanceElement;
import com.black.core.spring.instance.InstanceException;
import com.black.core.spring.instance.InstanceFactory;

import java.util.Map;

@SuppressWarnings("all")
public interface InstanceSingtonFactory extends ReflexFactory<Object> {

    <T> T getInstance(Class<T> instanceClass);


    /**
     * 获取一个实例话对象, 作为{@link InstanceFactory#getInstance(Class)}
     * 的一个重要的步骤存在
     * @param element 实例对象的处理封装类
     * @param <T> 实例 class 类型
     * @return 返回实例对象
     */
    default <T> T obtainInstance(InstanceElement<T> element){
        return obtainInstance(element, null);
    }


    /**
     * 获取一个实例话对象, 作为{@link InstanceFactory#getInstance(Class)}
     * 的一个重要的步骤存在
     * @param element 实例对象的处理封装类
     * @param constructorArgMap 构造器参数 map
     * @param <T> 实例 class 类型
     * @return 返回实例对象
     */
    <T> T obtainInstance(InstanceElement<T> element, Map<Class<?>, Object> constructorArgMap);


    //初始化 实例对象
    <T> T initializeInstance(T instance, Class<?> elementClass, Map<String, Object> otherSource);

    /**
     * initializeInstance 内部的一个子方法, 第二部的具体实现
     * @param instance 要填充的实例
     */
    void autoWriedWrapper(Object instance);

    /** 需要 spring 的 bean 来注入, 一般是被{@link javax.annotation.Resource}
     * 注解的字段
     * */
    void autoWriedBySpringMutes(Object instance);

    default <T> T initializeInstance(T instance, Class<?> elementClass){
        return initializeInstance(instance, elementClass, null);
    }

    /***
     * 获取构造器参数封装 map
     * @param instanceElement 实例化封装对象
     * @return 参数 map
     */
    default Map<Class<?>, Object> constructorArgsWrapper(InstanceElement<?> instanceElement){

        if (instanceElement == null){
            throw new InstanceException("instanceElement 不能为空");
        }
        return constructorArgsWrapper(instanceElement.instanceConstructorWrapper());
    }


    /***
     * 处理构造器参数
     * @param argsInstanceElementWrapper 构造器 map
     * @return 返回构造器需要的参数 map
     */
    Map<Class<?>, Object> constructorArgsWrapper(Map<Class<?>, InstanceElement<?>> argsInstanceElementWrapper);


}
