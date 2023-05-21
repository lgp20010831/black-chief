package com.black.core.factory.beans.config_collect520;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.tools.BeanUtil;
import com.black.utils.CollectionUtils;
import com.black.utils.NameUtil;
import com.black.utils.ServiceUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public abstract class AbstractCollectHandler {


    protected void calibrationCollectCondition(Class<?> type, Class<?>[] genericVal, CollectCondition collectCondition){
        if (Collection.class.isAssignableFrom(type)){
            if (genericVal.length == 1){
                Class<?> superType = genericVal[0];
                processCondition(superType, collectCondition);
            }
        }else if (Map.class.isAssignableFrom(type)){
            if (genericVal.length == 2){
                Class<?> superType = genericVal[1];
                processCondition(superType, collectCondition);
            }
        }else {
            processCondition(type, collectCondition);
            collectCondition.setSingle(true);
        }
    }


    protected void processCondition(Class<?> superType, CollectCondition collectCondition){
        if (superType.equals(Object.class)){
            return;
        }else if (superType.equals(Class.class)){
            collectCondition.setInstance(false);
        }else {
            Class<?>[] conditionTypes = collectCondition.getType();
            if (conditionTypes == null ||conditionTypes.length == 0){
                collectCondition.setType(new Class[]{superType});
            }
        }
    }


    protected Object collectAndConvert(Class<?> type, Class<?>[] genericVal, CollectCondition collectCondition, BeanFactory beanFactory){
        if (!(beanFactory instanceof ResourceCollectionBeanFactory)){
            throw new IllegalStateException("current bean factory is not ResourceCollectionBeanFactory");
        }

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
            if (genericVal.length == 2){
                Class<?> keyType = genericVal[0];
                if (keyType.equals(String.class)){
                    state = 1;
                }else {
                    state = 0;
                }
            }
            for (Object resource : resources) {
                if (state == 0){
                    map.put(BeanUtil.getPrimordialClass(resource), resource);
                }else if (state == 1){
                    map.put(NameUtil.getName(resource), resource);
                }
            }
            return map;
        }else {
            Object element = CollectionUtils.firstElement(resources);
            return element;
        }
    }
}
