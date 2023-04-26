package com.black.core.spring.instance;

import java.lang.reflect.Constructor;
import java.util.Map;

public interface InstanceElement<T> {

    /** 实例的 class 对象 */
    Class<T> instanceClass();

    /** 该实例继承的所有类 */
    Class<?>[] superClassWrapper();

    /** 该实例所实现的所有接口 */
    Class<?>[] interfaceWrapper();

    /** 期望的构造器, 多个构造器之间通过标识注解 {@link InstanceConstructor} 来选择,
     *  默认为空构造器, 如果返回结果为空, 则表示没有找到合适的构造器 */
    Map<Class<?>, InstanceElement<?>> instanceConstructorWrapper();

    /** 返回构造器 */
    Constructor<T> instanceConstructor();

    /** 该实例是否存在于 spring 容器中 */
    boolean springComponent();

    /** 该实例是否为 spring 的配置类 */
    boolean springConfig();


}
