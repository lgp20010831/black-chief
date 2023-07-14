package com.black.core.aop.servlet.nill;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.Trust;
import com.black.core.util.StringUtils;
import com.black.pattern.PropertyReader;
import com.black.utils.ServiceUtils;

import java.util.*;

/**
 * @author 李桂鹏
 * @create 2023-07-12 15:26
 */
@SuppressWarnings("all") @GlobalAround
public class NoTextAsNullResolver implements GlobalAroundResolver {

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper mw) {
        if (mw.parameterHasAnnotation(NoTextAsNull.class)){
            List<ParameterWrapper> parameterWrapperList = mw.getParameterByAnnotation(NoTextAsNull.class);
            for (ParameterWrapper parameterWrapper : parameterWrapperList) {
                NoTextAsNull annotation = parameterWrapper.getAnnotation(NoTextAsNull.class);
                Object target = args[parameterWrapper.getIndex()];
                Object asNull = handleNoTextAsNull(target, annotation.removeMapKey());
                args[parameterWrapper.getIndex()] = asNull;
            }
        }
        return args;
    }

    public static Object handleNoTextAsNull(Object target, boolean removeMapKey){
        if (target == null) return null;
        if (target instanceof String){
            if (!StringUtils.hasText((String) target)){
                return null;
            }
            return target;
        }
        if (target instanceof Map){
            Set<Object> inNullKeys = new HashSet<>();
            ((Map<?, ?>) target).keySet().removeIf(key -> {
                Object val = ((Map<?, ?>) target).get(key);
                if (val == null) return false;
                Object asNull = handleNoTextAsNull(val, removeMapKey);
                if (asNull == null){
                    if (removeMapKey){
                        return true;
                    }else {
                        inNullKeys.add(key);
                    }
                }
                return false;
            });
            for (Object inNullKey : inNullKeys) {
                ((Map<Object, ?>) target).put(inNullKey, null);
            }
            return target;
        }

        if (target instanceof Collection){
            Collection<Object> collection = (Collection<Object>) target;
            Collection<Object> copy = ServiceUtils.createCollection(target.getClass());
            for (Object o : collection) {
                copy.add(handleNoTextAsNull(o, removeMapKey));
            }
            collection.clear();
            collection.addAll(copy);
            return collection;
        }

        if (!target.getClass().isAnnotationPresent(Trust.class)){
            return target;
        }

        PropertyReader.visitProperties(target, property -> {
            property.set(handleNoTextAsNull(property.get(), removeMapKey));
        });
        return target;
    }
}
