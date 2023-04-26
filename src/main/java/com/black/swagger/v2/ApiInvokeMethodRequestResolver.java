package com.black.swagger.v2;

import com.black.api.ApiRequestResolver;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.utils.ServiceUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ApiInvokeMethodRequestResolver extends ApiRequestResolver {

    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    @Override
    protected Class<?> toFun(String alias, Class<?> assist) {
        if (assist == null){
            return super.toFun(alias, assist);
        }
        if (alias.startsWith("$<")){
            AtomicReference<Object> beanRef = new AtomicReference<>();
            ClassWrapper<?> wrapper = ClassWrapper.get(assist);
            alias = ServiceUtils.parseTxt(alias, "$<", ">", name -> {
                MethodWrapper method = wrapper.getSingleMethod(name);
                if (method != null){
                    Object bean = beanRef.get();
                    if (bean == null){
                        beanRef.set(cache.computeIfAbsent(assist, type -> {
                            return InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                        }));
                        bean = beanRef.get();
                        Object invoke = method.invoke(bean);
                        if (invoke != null){
                            return invoke.toString();
                        }
                    }
                }
                return "";
            });
        }
        return super.toFun(alias, assist);
    }
}
