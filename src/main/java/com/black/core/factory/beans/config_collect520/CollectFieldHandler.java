package com.black.core.factory.beans.config_collect520;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.query.FieldWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.utils.ReflectionUtils;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("all")
public class CollectFieldHandler extends AbstractCollectHandler implements BeanInitializationHandler {

    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        return fw.hasAnnotation(Collect.class);
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {
        Collect annotation = fw.getAnnotation(Collect.class);
        CollectCondition collectCondition = AnnotationUtils.loadAttribute(annotation, new CollectCondition());
        Class<?> type = fw.getType();
        Class<?>[] genericVal = ReflectionUtils.genericVal(fw.get(), type);
        calibrationCollectCondition(type, genericVal, collectCondition);
        Object source = collectAndConvert(type, genericVal, collectCondition, factory);
        Object value = fw.getValue(bean);
        if (value == null){
            fw.setValue(bean, source);
        }else {
            if (Collection.class.isAssignableFrom(type)){
                ((Collection<Object>)value).addAll((Collection<Object>) source);
            }else if (Map.class.isAssignableFrom(type)){
                ((Map<Object, Object>)value).putAll((Map<?, ?>) source);
            }
        }
    }
}
