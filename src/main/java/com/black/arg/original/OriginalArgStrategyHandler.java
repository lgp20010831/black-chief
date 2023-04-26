package com.black.arg.original;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Map;

public interface OriginalArgStrategyHandler {

    boolean support(OriginalArgStrategy strategy);

    Object handler(MethodWrapper mw, ParameterWrapper pw, Map<String, Object> originalArgMap, MethodReflectionIntoTheParameterProcessor parameterProcessor);


    //是否可以继续解析这个参数
    default boolean canNext(Object arg){
        return arg == null;
    }

}
