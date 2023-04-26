package com.black.arg.original;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.query.MethodWrapper;

import java.util.Map;

public abstract class AbstractCompatibleIndexHandler implements OriginalArgStrategyHandler {


    @Override
    public Object handler(MethodWrapper mw, ParameterWrapper pw, Map<String, Object> originalArgMap, MethodReflectionIntoTheParameterProcessor parameterProcessor) {
        Object[] args = originalArgMap.values().toArray();
        int index = pw.getIndex();
        if (index > args.length -1){
            return null;
        }
        Object arg = args[index];
        return TypeConvertCache.initAndGet().convert(pw.getType(), arg);
    }
}
