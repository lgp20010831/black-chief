package com.black.core.aop.code;

import com.black.core.aop.annotation.HybridSort;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;

public class InterceptInitialInformation {

    //节点
    AopTaskManagerHybrid hybrid;

    //节点对应的拦截器
    AopTaskIntercepet intercepet;

    //顺序
    int order = 250;

    Map<Class<?>, Collection<Method>> mappingCondition = new HashMap<>();

    public InterceptInitialInformation(AopTaskManagerHybrid hybrid, AopTaskIntercepet intercepet) {
        this.hybrid = hybrid;
        this.intercepet = intercepet;
        Class<AopTaskManagerHybrid> primordialClass = BeanUtil.getPrimordialClass(hybrid);
        HybridSort hybridSort = AnnotationUtils.findAnnotation(primordialClass, HybridSort.class);
        if (hybridSort != null){
            order = hybridSort.value();
        }
    }

    public InterceptInitialInformation(AopTaskIntercepet intercepet){
        this.intercepet = intercepet;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void registerClass(Class<?> c){
        if (!mappingCondition.containsKey(c)) {
            mappingCondition.put(c, new HashSet<>());
        }
    }

    public void registerMethod(Class<?> clazz, Method method){
        Collection<Method> methods = mappingCondition.computeIfAbsent(clazz, c -> new HashSet<>());
        methods.add(method);
    }

    public AopTaskManagerHybrid getHybrid() {
        return hybrid;
    }

    public AopTaskIntercepet getIntercepet() {
        return intercepet;
    }

    public Map<Class<?>, Collection<Method>> getMappingCondition() {
        return mappingCondition;
    }

    public int getOrder() {
        return order;
    }
}
