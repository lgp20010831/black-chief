package com.black.core.factory.beans.config;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.BeansUtils;
import com.black.core.tools.BeanUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertiesWiredManager {


    public static Object wiredObject(Map<String, String> pro, Object object, boolean methodAdd){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(object);
        if (primordialClass.isArray()){
            throw new BeanFactorysException("property cannot be injected into an array");
        }
        if (object instanceof Map){
            Map<String, Object> map = (Map<String, Object>) object;
            map.putAll(pro);
        }else if (object instanceof Collection){
            Collection<Object> collection = (Collection<Object>) object;
            collection.addAll(pro.values());
        }else {
            BeansUtils.wriedPropertiesBean(object, pro, methodAdd);
        }

        return object;
    }

    public static Object wiredNullObject(Map<String, String> pro, Class<?> type, boolean methodAdd, BeanFactory beanFactory){
        Object object;
        if (Map.class.isAssignableFrom(type)){
            object = new LinkedHashMap<>();
        }else if (Collection.class.isAssignableFrom(type)){
            object = new ArrayList<>();
        }else {
            object = beanFactory.prototypeCreateBean(type);
        }
        return wiredObject(pro, object, methodAdd);
    }

}
