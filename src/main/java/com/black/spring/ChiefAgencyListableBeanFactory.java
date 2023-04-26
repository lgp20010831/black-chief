package com.black.spring;

import com.black.callback.ApplicationStartingTaskManager;
import com.black.callback.CallBackRegister;
import com.black.spring.agency.AgencyScanClass;
import com.black.spring.agency.ObjectAgencyRegister;
import com.black.spring.mapping.UrlMappingHandler;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.LifecycleProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class ChiefAgencyListableBeanFactory extends DefaultListableBeanFactory {

    public static final Set<Class<?>> BLACK = new HashSet<>();

    public static void registerBlack(Class<?> type){
        BLACK.add(type);
    }

    static {
        BLACK.add(ChiefExpansivelyApplication.class);
        BLACK.add(DefaultListableBeanFactory.class);
        BLACK.add(LifecycleProcessor.class);
        ApplicationStartingTaskManager.addTask(AgencyScanClass::present);
        CallBackRegister.addTask(() -> {
            DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
            RequestMappingHandlerMapping mapping = beanFactory.getBean(RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            for (RequestMappingInfo mappingInfo : handlerMethods.keySet()) {
                HandlerMethod handlerMethod = handlerMethods.get(mappingInfo);
                UrlMappingHandler.flushHttpMethod(mappingInfo, handlerMethod.getBeanType(), handlerMethod.getMethod());
            }
        });
    }

    /*
        实现对 bean factory 注册的对象进行代理
        能够让外部程序自由的重写
        bean 的方法

        需要单独写一个组件 扫描自定义的重写

        @Rewrite(bean = BeanFactory.class, methodName = "registerSingleton")
        public Object writeRegisterSingleton(String beanName, Object bean){
            //....
        }


        //接下来增强 控制器的重写, 比如在 jar 包存在一个接口 /user/list  因为在jar包所以无法重写
        便可以通过此方法进行重写

        @UrlMapping("/user/list")
        public Object rewriteUserList(JSONObject json){
            //do fetch....
        }


        普通: agency proxy -> cglib -> controller
        by chief: agency proxy -> cglib -> apply proxy -> controller
        优化后 cglib -> apply proxy -> controller

     */

    @Override
    protected void addSingleton(String beanName, Object singletonObject) {
        Object bean = singletonObject;
        if (singletonObject instanceof BeanEnhanceWrapper){
            bean = ((BeanEnhanceWrapper) singletonObject).getBean();
        }
        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        //如果要求代理全部 或者 注册中心要求的类 或者 是个控制器
        if (register.isProxyAll() || register.isSupport(bean) || isController(bean)) {
            //如果该对象在代理范围内 并且不再黑名单 并且支持cglib 代理
            if (register.inRange(bean) && !isBlack(bean) && register.isCglibProxy(bean)){
                bean = register.proxyInstance(bean);
                if (singletonObject instanceof BeanEnhanceWrapper){
                    ((BeanEnhanceWrapper) singletonObject).setBean(bean);
                }
            }else {
                //不支持进行 cglib 代理, 则不会进行代理
                //System.out.println("没有资格进行 cglib 代理: " + bean);
            }
        }
        super.addSingleton(beanName, bean);
    }

    public boolean isBlack(Object instance){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(instance);
        for (Class<?> b : BLACK) {
            if (b.isAssignableFrom(primordialClass)){
                return true;
            }
        }
        return false;
    }

    public boolean isController(Object bean){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        if (BasicErrorController.class.equals(primordialClass)){
            return false;
        }
        return AnnotationUtils.isPertain(primordialClass, Controller.class) || AnnotationUtils.isPertain(primordialClass, RequestMapping.class);
    }

}
