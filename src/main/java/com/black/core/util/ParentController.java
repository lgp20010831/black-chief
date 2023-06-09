package com.black.core.util;

import com.black.core.aop.servlet.AopControllerIntercept;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class ParentController implements BeanFactoryAware {

    protected BeanFactory factory;
    protected final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    protected <M> M getBean(@NonNull Class<M> beanType){
        Assert.notNull(factory, "factory 异常为空");
        if (cache.containsKey(beanType)){
            return (M) cache.get(beanType);
        }
        try {
            M bean = factory.getBean(beanType);
            cache.put(beanType, bean);
            return bean;
        }catch (BeansException be){
            throw new IllegalStateException("无法获取 bean 对象: " + beanType.getSimpleName());
        }
    }

    public HttpServletRequest getRequest(){
        return AopControllerIntercept.getRequest();
    }

    public HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public String getPathPart(int index){
        String servletPath = getRequest().getServletPath();
        String[] splits = servletPath.split("/");
        return splits[index];
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    public BeanFactory getFactory() {
        return factory;
    }

}
