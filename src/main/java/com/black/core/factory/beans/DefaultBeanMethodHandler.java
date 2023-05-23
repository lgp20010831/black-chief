package com.black.core.factory.beans;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.annotation.SingleBean;
import com.black.core.factory.beans.lazy.KeyUtils;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.utils.NameUtil;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultBeanMethodHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return (parameter.getAnnotations().isEmpty() || parameter.hasAnnotation(SingleBean.class))
                && !ClassWrapper.isBasic(parameter.getType().getName());
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            try {
                Parameter param = parameter.get();
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(param);
                Class<?> type = parameter.getType();
                if (Collection.class.isAssignableFrom(type)){
                    if (genericVals.length != 1){
                        throw new BeanFactorysException("When attempting to inject a collection property, " +
                                "its generic type must be specified");
                    }
                    Class<?> rawType = genericVals[0];
                    List<?> list = factory.getBean(rawType);
                    Collection<Object> collection = ServiceUtils.createCollection(type);
                    collection.addAll(list);
                    return collection;
                }else if (Map.class.isAssignableFrom(type)){
                    if (genericVals.length != 2){
                        throw new BeanFactorysException("When attempting to inject a map property, " +
                                "its generic type must be specified");
                    }
                    Class<?> rawType = genericVals[1];
                    Class<?> keyType = genericVals[0];
                    List<?> list = factory.getBean(rawType);
                    Map<Object, Object> map = ServiceUtils.createMap(type);
                    if (keyType.equals(String.class)){
                        Map<String, ?> keyMap = KeyUtils.handlerKey(param, list);
                        map.putAll(keyMap);
                    }else {
                        for (Object ele : list) {
                            map.put(BeanUtil.getPrimordialClass(ele), ele);
                        }
                    }
                    return map;
                }

                return factory.getSingleBean(type);
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("An exception occurred while trying to " +
                        "create a parameter from the factory while parsing an object method " +
                        "parameter, Type of parameter to create: [" + parameter.getType() + "], " +
                        "object of delegation: [" + bean + "], analytical method: [" + method.getName() + "]" , ex);
            }
        }
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            try {

                return factory.getSingleBean(pw.getType());
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("An exception occurred while trying to " +
                        "create a parameter from the factory while parsing an object method " +
                        "parameter, Type of parameter to create: [" + pw.getType() + "], " +
                        "analytical constructor: [" + cw.getConstructor()+ "]" , ex);
            }
        }
        return previousValue;
    }
}
