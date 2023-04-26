package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;

import java.util.LinkedList;

public interface PostBeforeBeanInstantiationDriver extends Driver{

    LinkedList<PostBeforeBeanInstantiationDriver> invokerQueue = new LinkedList<>();

    default void setInvoker(PostBeforeBeanInstantiationDriver postBeforeBeanInstantiationDriver){
        invokerQueue.addLast(postBeforeBeanInstantiationDriver);
    }

    /** spring util 会执行该方法, 作为子类需要重写postBeforeBeanInstantiationLogic
     * 并向 spring util 中的PostBeforeBeanInstantiationDriver 调用 setInvoker 注册自己 */
    default Object postBeforeBeanInstantiation(Class<?> beanClass, String beanName,
                                               ChiefExpansivelyApplication chiefExpansivelyApplication){

        Object bean = null;
        PostBeforeBeanInstantiationDriver previousDriver = null;
        for (PostBeforeBeanInstantiationDriver instantiationDriver : invokerQueue) {

            bean = instantiationDriver.postBeforeBeanInstantiationLogic(beanClass, beanName,
                    chiefExpansivelyApplication, bean, previousDriver);

            previousDriver = instantiationDriver;
        }
        return bean;
    }

    Object postBeforeBeanInstantiationLogic(Class<?> beanClass, String beanName,
                                       ChiefExpansivelyApplication chiefExpansivelyApplication,
                                       Object previousResultBean,
                                       PostBeforeBeanInstantiationDriver previousDriver);
}
