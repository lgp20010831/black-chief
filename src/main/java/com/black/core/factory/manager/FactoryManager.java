package com.black.core.factory.manager;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.component.BeanFactoryType;
import com.black.core.factory.beans.xml.XmlBeanFactory;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.factory.DefaultProxyFactory;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.DefaultInstanceElementFactory;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.spring.instance.LightnessInstanceFactory;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

public class FactoryManager {

    private static InstanceFactory instanceFactory;

    private static BeanFactory beanFactory;

    private static ReusingProxyFactory proxyFactory;

    private static final Class<? extends BeanFactory> DEFAULT_BEAN_FACTORY_TYPR = XmlBeanFactory.class;

    public static InstanceFactory initAndGetInstanceFactory() {
        init();
        return instanceFactory;
    }


    public static InstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public static BeanFactory initAndGetBeanFactory() {
        init();
        return beanFactory;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static ReusingProxyFactory initAndGetProxyFactory() {
        init();
        return proxyFactory;
    }


    public static ReusingProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public static void setBeanFactory(BeanFactory beanFactory) {
        FactoryManager.beanFactory = beanFactory;
    }

    public static void setInstanceFactory(InstanceFactory instanceFactory) {
        FactoryManager.instanceFactory = instanceFactory;
    }

    public static void setProxyFactory(ReusingProxyFactory proxyFactory) {
        FactoryManager.proxyFactory = proxyFactory;
    }

    public static synchronized void init(){
        createInstanceFactory();
        createDefaultProxyFactory();
        createDefaultBeanFactory();
    }

    public static void createDefaultProxyFactory(){
        if (instanceFactory == null){
            createInstanceFactory();
        }

        if (proxyFactory == null){
            proxyFactory = instanceFactory.getInstance(DefaultProxyFactory.class);
        }
    }

    public static void createDefaultBeanFactory(){
        if (instanceFactory == null){
            createInstanceFactory();
        }

        if (beanFactory != null){
            return;
        }
        Class<? extends BeanFactory> factoryType = DEFAULT_BEAN_FACTORY_TYPR;
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        if (mainClass != null){
            BeanFactoryType beanFactoryType = AnnotationUtils.getAnnotation(mainClass, BeanFactoryType.class);
            if (beanFactoryType != null){
                Class<? extends BeanFactory> type = beanFactoryType.value();
                if (!BeanUtil.isSolidClass(type)){
                    throw new BeanFactorysException("When specifying the type of bean factory, " +
                            "it cannot be an interface, abstract class and other unspecified classes");
                }
                factoryType = type;
            }
        }
        beanFactory = instanceFactory.getInstance(factoryType);
    }


    public static void createInstanceFactory(){
        if (instanceFactory == null){
            instanceFactory = new LightnessInstanceFactory(new DefaultInstanceElementFactory());
        }
    }
}
