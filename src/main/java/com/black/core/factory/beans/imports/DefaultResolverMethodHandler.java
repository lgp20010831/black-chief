package com.black.core.factory.beans.imports;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.cache.TypeConvertCache;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-29 16:44
 */
@SuppressWarnings("all")
public class DefaultResolverMethodHandler implements BeanMethodHandler {

    @Override
    public boolean supportReturnProcessor(MethodWrapper method, Object returnValue) {
        return !method.getReturnType().equals(void.class) && method.hasAnnotation(Default.class);
    }

    @Override
    public Object processor(MethodWrapper method, Object returnValue, Object bean, BeanFactory factory) {
        Default annotation = method.getAnnotation(Default.class);
        if (annotation != null && returnValue == null){
            Class<?> returnType = method.getReturnType();
            String defaultTxt = annotation.value();
            if (StringUtils.hasText(defaultTxt)){
                returnValue = TypeConvertCache.initAndGet().convert(returnType, defaultTxt);
            }else {
                if (Collection.class.isAssignableFrom(returnType)){
                    return ServiceUtils.createCollection(returnType);
                }

                if (Map.class.isAssignableFrom(returnType)){
                    return ServiceUtils.createMap(returnType);
                }

                if (returnType.equals(String.class)){
                    return "";
                }

                if (returnType.equals(Integer.class)){
                    return 0;
                }

                if (returnType.equals(Long.class)){
                    return 0L;
                }

                if (returnType.equals(Double.class)){
                    return 0D;
                }

                if (returnType.equals(Short.class)){
                    return Short.parseShort("0");
                }

                if (returnType.equals(Character.class)){
                    return '0';
                }

                if (returnType.equals(Byte.class)){
                    return (byte)0;
                }

                if (returnType.equals(Boolean.class)){
                    return false;
                }

                if (returnType.equals(Float.class)){
                    return 0f;
                }

                return InstanceBeanManager.instance(returnType, InstanceType.REFLEX_AND_BEAN_FACTORY);
            }
        }
        return returnValue;
    }
}
