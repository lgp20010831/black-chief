package com.black.core.factory.beans.config_collect520;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.generic.Generic;
import com.black.generic.GenericInfo;
import com.black.utils.CollectionUtils;
import com.black.utils.NameUtil;
import com.black.utils.ServiceUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public abstract class AbstractCollectHandler {


    protected void calibrationCollectCondition(Class<?> type, GenericInfo genericInfo, CollectCondition collectCondition){
        int size = genericInfo.size();
        if (Collection.class.isAssignableFrom(type)){
            if (size == 1){
                Generic generic = genericInfo.getGeneric(0);
                processCondition(generic, collectCondition);
            }
        }else if (Map.class.isAssignableFrom(type)){
            if (size == 2){
                Generic generic = genericInfo.getGeneric(1);
                processCondition(generic, collectCondition);
            }
        }else {

            processCondition(genericInfo.isEmpty() ? null : genericInfo.getGeneric(0),
                    collectCondition);
            collectCondition.setSingle(true);
        }
    }


    protected void processCondition(Generic generic, CollectCondition collectCondition){
        if (generic == null){
            return;
        }
        Class<?> genericType = generic.getGenericType();
        if (genericType.equals(Object.class)){
            return;
        }else if (genericType.equals(Class.class)){
            if (generic.hasMore()){
                Class<?> deepGeneric = generic.getDeepGeneric();
                Class<?>[] rawTypes = collectCondition.getType();
                Class<?>[] array = ServiceUtils.addClassArray(rawTypes, deepGeneric);
                collectCondition.setType(array);
            }
            collectCondition.setInstance(false);
        }else {
            collectCondition.setInstance(true);
            Class<?>[] conditionTypes = collectCondition.getType();
            Class<?>[] array = ServiceUtils.addClassArray(conditionTypes, genericType);
            collectCondition.setType(array);
        }
    }


    protected Object collectAndConvert(Class<?> type, GenericInfo genericInfo, CollectCondition collectCondition, BeanFactory beanFactory){
        if (!(beanFactory instanceof ResourceCollectionBeanFactory)){
            throw new IllegalStateException("current bean factory is not ResourceCollectionBeanFactory");
        }
        int size = genericInfo.size();
        ResourceCollectionBeanFactory resourceCollectionBeanFactory = (ResourceCollectionBeanFactory) beanFactory;
        List<Object> resources = resourceCollectionBeanFactory.collect(collectCondition);
        if (Collection.class.isAssignableFrom(type)){
            Collection<Object> collection = ServiceUtils.createCollection(type);
            collection.addAll(resources);
            return collection;
        }else if (Map.class.isAssignableFrom(type)){
            Map<Object, Object> map = ServiceUtils.createMap(type);
            //0: asClass, 1: asName
            int state = 0;
            if (size == 2){
                Class<?> keyType = genericInfo.getGeneric(0).getGenericType();
                if (keyType.equals(String.class)){
                    state = 1;
                }else {
                    state = 0;
                }
            }
            String key = collectCondition.getKey();
            for (Object resource : resources) {
                if (state == 0){
                    map.put(BeanUtil.getPrimordialClass(resource), resource);
                }else if (state == 1){
                    if (!StringUtils.hasText(key)){
                        map.put(NameUtil.getName(resource), resource);
                    }else {
                        //构造环境
                        Map<String, Object> env = new LinkedHashMap<>();
                        env.put("source", resource);
                        Class<Object> resourceType = BeanUtil.getPrimordialClass(resource);
                        env.put("sourceType", resourceType);
                        Class<? extends Annotation>[] annotationAt = collectCondition.getAnnotationAt();
                        for (Class<? extends Annotation> at : annotationAt) {
                            Annotation annotation = AnnotationUtils.findAnnotation(resourceType, at);
                            env.put(at.getSimpleName(), annotation);
                        }
                        String mapKey = ServiceUtils.patternGetValue(env, key);
                        map.put(mapKey, resource);
                    }

                }
            }
            return map;
        }else {
            Object element = CollectionUtils.firstElement(resources);
            return element;
        }
    }
}
