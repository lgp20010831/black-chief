package com.black.core.spring.instance;

import com.black.core.spring.factory.Factory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

public interface InstanceFactory extends Factory {
    /**
     * 获取一个实例的对象, 传递的 class 不能为空
     * 如果工厂内部存在该 class 类型的实例(判断的条件是
     * 工厂内部存在跟此 class 有直接关联的对象), 直接返回
     * 否则会进行创建, 通过注解选择合适的构造器, 并将构造方法
     * 的参数通过 getInstance 来获取
     * 如果存在多个类型的对象, 则默认返回的是其中一个, 所有需要
     * {@link InstanceFactory#getInstanceMutes(Class)}
     * @param instanceClass 实例 class 对象
     * @param <T> 类型
     * @return 实例对象
     */
    <T> T getInstance(Class<T> instanceClass);

    /***
     * 获取这个类型下的所有可能存在的类型
     * @param condition 条件 class 对象
     * @param <T> 指定类型
     * @return 返回结果, 可能为空
     */
    <T> List<? extends T> getInstanceMutes(Class<T> condition);

    /**
     * 直接向工厂内部缓存注册一个实例对象
     * 注册的 key 和 value 等于传递的参数
     * 返回值只是判断了他有没有跟他存在继承关系的
     * 实例, 返回 true 表示存在, false 表示不存在
     * 均不能为空
     * @param instanceClass 实例 class 对象
     * @param instance 实例对象
     * @return 返回判断结果
     */
    <K, V extends K> boolean registerInstance(Class<K> instanceClass, V instance);

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

    /**
     * 实例化, 经过前面的准备来实现具体的实例化
     * @param element 元素对象
     * @param constructorArgMap 构造器 map
     * @param <T> 类型
     * @return 实例化对象
     */
    <T> T instance(InstanceElement<T> element, Map<Class<?>, Object> constructorArgMap);


    InstanceElementFactory getElementFactory();

    /** 获取合适的构造器 **/
    <T> Constructor<T> obtainConstructor(InstanceElement<T> instanceElement);

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

    default <T> T initializeInstance(T instance, Class<?> elementClass){
        return initializeInstance(instance, elementClass, null);
    }

    /**
     * 初始化一个实例, 会通过两个方面进行初始化
     * 1. 通过 spring BeanFactory 进行填充 spring bean
     *    属性, 如果工厂内不存在 spring 依赖, 则会跳过该过程
     * 2. 通过{@link Reflex} 注解, 来通过工厂获取该对象
     * @param instance 要初始化的实例
     * @param elementClass 实例 class
     * @param otherSource 如果需要填充其字段的其他属性
     *                    可以通过 字段名 --> 值 来注入
     * @param <T> 实例类型
     * @return 返回实例对象
     */
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


    void setSpringBeanFactory(DefaultListableBeanFactory beanFactory);

    /** 返回工厂内部存贮实例的 map */
    Map<Class<?>, Object> getMutes();

    /**
     * 删除一个实例的方法
     * @param targetClass 目标的 class 对象
     * @return 如果为 false 表示删除失败
     */
    boolean remove(Class<?> targetClass);
}
