package com.black.core.servlet.intercept;

import com.black.core.config.SpringAutoConfiguration;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.InstanceFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
@LoadSort(25)
public class ServletInterceptTaskChain implements OpenComponent, PostPatternClazzDriver, ApplicationDriver {

    private HandlerInterceptor interceptor;

    private final Collection<Class<HandlerInterceptor>> interceptorClasses = new ArrayList<>();

    public void registerInterceptor(HandlerInterceptor handlerInterceptor){
        if (interceptor instanceof ServletInterceptor){
            ServletInterceptor servletInterceptor = (ServletInterceptor) interceptor;
            servletInterceptor.registerInterceptor(handlerInterceptor);
        }
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

        Collection<Object> mutes = expansivelyApplication.getApplicationConfigurationMutes();
        for (Object mute : mutes) {
            if (mute instanceof SpringAutoConfiguration){
                SpringAutoConfiguration configuration = (SpringAutoConfiguration) mute;
                interceptor = configuration.getHandlerInterceptor();
            }
        }
        if (interceptor instanceof ServletInterceptor){
            ServletInterceptor servletInterceptor = (ServletInterceptor) interceptor;
            InstanceFactory instanceFactory = expansivelyApplication.instanceFactory();
            for (Class<HandlerInterceptor> interceptorClass : interceptorClasses) {
                servletInterceptor.registerInterceptor(instanceFactory.getInstance(interceptorClass));
            }

        }
    }

    @Override
    public void postPatternClazz(Class<?> beanClazz, Map<Class<? extends OpenComponent>, Object> springLoadComponent, ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        if (beanClazz.isInterface() || beanClazz.isEnum() || Modifier.isAbstract(beanClazz.getModifiers())){
            return;
        }
        MvcIntercept mvcIntercept;
        if (HandlerInterceptor.class.isAssignableFrom(beanClazz) &&
                (mvcIntercept = AnnotationUtils.getAnnotation(beanClazz, MvcIntercept.class)) != null){
            if (!interceptorClasses.contains(beanClazz)){
                interceptorClasses.add((Class<HandlerInterceptor>) beanClazz);
            }
        }
    }
}
