package com.black.mvc;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.holder.SpringHodler;
import com.black.spring.BeanEnhanceWrapper;
import com.black.spring.ChiefAgencyListableBeanFactory;
import com.black.core.aop.weaving.AopWeavingRegister;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.tools.BeanUtil;
import lombok.NonNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class MvcMappingRegister {

    private static final IoLog log = LogFactory.getArrayLog();

    public static Object registerSupportAopController(Class<?> type){
        prepare(type);
        Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        autowiredBean(instance);
        AopWeavingRegister weavingRegister = AopWeavingRegister.getInstance();
        weavingRegister.register(type);
        weavingRegister.flush();
//        Object jdkProxy = AdvisorWeavingFactory.proxy(instance);
//        Object proxy = ApplyProxyFactory.proxy(jdkProxy, new SupportAopProxyLayer(type));
        Object jdkProxy = ApplyProxyFactory.proxy(instance, new SupportAopProxyLayer(type));
        Object proxy = AdvisorWeavingFactory.proxy(jdkProxy, false);
        registerController(proxy);
        return proxy;
    }

    public static Object registerCommonController(Class<?> type){
        prepare(type);
        Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        autowiredBean(instance);
        registerController(instance);
        return instance;
    }

    public static void autowiredBean(Object bean){
        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        beanFactory.autowireBean(bean);
    }

    public static void prepare(Class<?> type){
        ChiefExpansivelyApplication expansivelyApplication = ChiefApplicationHolder.getExpansivelyApplication();
        if(expansivelyApplication != null){
            expansivelyApplication.getProjectClasses().add(type);
        }
    }

    public static void registerController(@NonNull Object controller){
        log.debug("[MvcMappingRegister] target class is {}", controller.getClass());
        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        RequestMappingHandlerMapping handlerMapping = beanFactory.getBean(RequestMappingHandlerMapping.class);
        ClassWrapper<RequestMappingHandlerMapping> cw = ClassWrapper.get(BeanUtil.getPrimordialClass(handlerMapping));
        MethodWrapper mw = cw.getMethod("detectHandlerMethods", Object.class);
        log.trace("[MvcMappingRegister] register controller ===> {}", controller);
        DefaultListableBeanFactory listableBeanFactory = SpringHodler.getListableBeanFactory();
        if (listableBeanFactory instanceof ChiefAgencyListableBeanFactory){
            BeanEnhanceWrapper enhanceWrapper = new BeanEnhanceWrapper(controller);
            SpringBeanRegister.registerBean(enhanceWrapper, false);
            controller = enhanceWrapper.getBean();
        }else {
            SpringBeanRegister.registerBean(controller, false);
        }
        mw.invoke(handlerMapping, controller);
    }


}
