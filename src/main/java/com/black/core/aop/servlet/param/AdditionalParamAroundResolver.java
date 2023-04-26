package com.black.core.aop.servlet.param;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@GlobalAround
public class AdditionalParamAroundResolver implements GlobalAroundResolver {

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper mw) {
        MethodWrapper methodWrapper = mw.getMethodWrapper();
        if (methodWrapper.hasAnnotation(AdditionalParamBody.class) || methodWrapper.parameterHasAnnotation(AdditionalParamBody.class)){
            handlerArgs0(args, methodWrapper, mw);
        }
        return args;
    }

   private void handlerArgs0(Object[] args, MethodWrapper mw, HttpMethodWrapper hmw){
        AdditionalParamBody ann = mw.getAnnotation(AdditionalParamBody.class);
        boolean global = ann != null;
        ParamBodyHandler globalBodyHandler = global ? ReflexUtils.instance(ann.value()) : null;
        List<ParameterWrapper> pws = global ?  new ArrayList<>(mw.getParameterWrappersSet()) : mw.getParameterByAnnotation(AdditionalParamBody.class);
       for (ParameterWrapper pw : pws) {
           ParamBodyHandler bodyHandler;
           if (global){
               bodyHandler = globalBodyHandler;
           }else {
               bodyHandler = ReflexUtils.instance(pw.getAnnotation(AdditionalParamBody.class).value());
           }
           Object arg = args[pw.getIndex()];
           if (arg instanceof Collection){
               arg = bodyHandler.processorCollection(hmw, (Collection<?>) arg);
           }else if (arg instanceof Map){
               arg = bodyHandler.processorMap(hmw, (Map<?, ?>) arg);
           }else {
               arg = bodyHandler.processorUnknown(hmw, arg);
           }
           args[pw.getIndex()] = arg;
       }
    }
}
