package com.black.core.spring.driver;

import com.black.core.json.ReflexUtils;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.utils.ReflexHandler;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public interface ApplicationDriver extends Driver{

    default void whenApplicationStart(ChiefExpansivelyApplication application){}

    default void whenApplicationStop(ChiefExpansivelyApplication application){
        clearCache();
    }

    default void clearCache(Object component){
        if (component != null){
            for (Field field : ReflexHandler.getAccessibleFields(component, true)) {
                Class<?> type = field.getType();
                if (Map.class.isAssignableFrom(type)){
                    Map<?, ?> map = (Map<?, ?>) ReflexUtils.getValue(field, component);
                    if (map != null){
                        map.clear();
                    }
                }else if (Collection.class.isAssignableFrom(type)){
                    Collection<?> collection = (Collection<?>) ReflexUtils.getValue(field, component);
                    if(collection != null){
                        collection.clear();
                    }
                }
            }
        }
    }

    default void clearCache(){
        clearCache(this);
    }
}
