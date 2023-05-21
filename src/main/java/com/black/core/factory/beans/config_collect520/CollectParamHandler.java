package com.black.core.factory.beans.config_collect520;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.utils.ReflectionUtils;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("all")
public class CollectParamHandler extends AbstractCollectHandler implements BeanMethodHandler {


    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return parameter.hasAnnotation(Collect.class);
    }


    @Override
    public Object handler(MethodWrapper method, ParameterWrapper pw, Object bean, BeanFactory factory, Object previousValue) {
        Collect annotation = pw.getAnnotation(Collect.class);
        CollectCondition collectCondition = AnnotationUtils.loadAttribute(annotation, new CollectCondition());
        Class<?> type = pw.getType();
        Class<?>[] genericVal = ReflectionUtils.getMethodParamterGenericVals(pw.get());
        calibrationCollectCondition(type, genericVal, collectCondition);
        Object source = collectAndConvert(type, genericVal, collectCondition, factory);
        Object value = previousValue;
        if (value == null){
            return source;
        }else {
            if (Collection.class.isAssignableFrom(type)){
                ((Collection<Object>)value).addAll((Collection<Object>) source);
            }else if (Map.class.isAssignableFrom(type)){
                ((Map<Object, Object>)value).putAll((Map<?, ?>) source);
            }
            return value;
        }
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        Collect annotation = pw.getAnnotation(Collect.class);
        CollectCondition collectCondition = AnnotationUtils.loadAttribute(annotation, new CollectCondition());
        Class<?> type = pw.getType();
        Class<?>[] genericVal = ReflectionUtils.getMethodParamterGenericVals(pw.get());
        calibrationCollectCondition(type, genericVal, collectCondition);
        Object source = collectAndConvert(type, genericVal, collectCondition, factory);
        Object value = previousValue;
        if (value == null){
            return source;
        }else {
            if (Collection.class.isAssignableFrom(type)){
                ((Collection<Object>)value).addAll((Collection<Object>) source);
            }else if (Map.class.isAssignableFrom(type)){
                ((Map<Object, Object>)value).putAll((Map<?, ?>) source);
            }
            return value;
        }
    }
}
