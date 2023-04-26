package com.black.core.aop.servlet.param;

import com.black.core.aop.servlet.HttpMethodWrapper;

import java.util.Collection;
import java.util.Map;

public interface ParamBodyHandler {


    default Collection<?> processorCollection(HttpMethodWrapper httpMethodWrapper, Collection<?> collection){
        return collection;
    }

    default Map<?, ?> processorMap(HttpMethodWrapper httpMethodWrapper, Map<?, ?> map){
        return map;
    }

    default Object processorUnknown(HttpMethodWrapper httpMethodWrapper, Object result){
        return result;
    }

}
