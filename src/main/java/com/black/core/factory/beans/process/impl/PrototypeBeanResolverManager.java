package com.black.core.factory.beans.process.impl;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.json.ReflexUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.GenericWrapper;
import com.black.core.util.Assert;
import com.black.utils.ReflexHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrototypeBeanResolverManager {



    public static Object resolvePrototypeType(Class<?> type, int batch, BeanFactory beanFactory, GenericWrapper gw){
        if (Map.class.isAssignableFrom(type)){
            throw new BeanFactorysException("The prototype creation object does not support " +
                    "injecting map type properties, type is [" + type + "]");
        }
        String name = type.getName();
        if (ClassWrapper.isBasic(name) || ClassWrapper.isBasicWrapper(name)){
            throw new BeanFactorysException("The prototype creation object does not support injecting " +
                    "properties of primitive type or primitive type encapsulation type, type is [" + type + "]");
        }

        if (type.isArray()) {
            List<Object> beanList = createBatch(type, beanFactory, batch);
            return beanList.toArray();
        }

        if (List.class.isAssignableFrom(type)){
            Assert.notNull(gw, "GenericWrapper is null");
            List<Object> collection = type.equals(List.class) ? new ArrayList<>() : (List<Object>) ReflexUtils.instance(type);
            Class<?>[] genericVal = ReflexHandler.loopUpGenerics(gw.getGenericType(), type);
            if (genericVal.length != 1) {
                throw new BeanFactorysException("detailed generics need to be described when " +
                        "injecting into the prototype collection");
            }
            List<Object> objectList = createBatch(genericVal[0], beanFactory, batch);
            collection.addAll(objectList);
            return collection;
        }
        return beanFactory.prototypeCreateBean(type);
    }

    private static List<Object> createBatch(Class<?> type, BeanFactory beanFactory, int size){
        ArrayList<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(beanFactory.prototypeCreateBean(type));
        }
        return list;
    }

}
