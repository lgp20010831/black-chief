package com.black.core.sql.action;


import com.black.core.spring.factory.CGlibAndJDKProxyFactory;
import com.black.core.spring.factory.DefaultCGlibAndJDKProxyFactory;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.utils.ReflexHandler;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

@SuppressWarnings("all")
public abstract class AutoMapperController<M extends GlobalParentMapping> extends DynamicController{

    public static CGlibAndJDKProxyFactory proxyFactory = new DefaultCGlibAndJDKProxyFactory();

    protected final M mapper;

    private M suchMapper;

    protected Class<? extends GlobalParentMapping> type;

    protected AutoMapperController() {

        mapper = (M) proxyFactory.proxyJDK(findType(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (suchMapper == null){
                    suchMapper = findMapper();
                }
                if (suchMapper == null){
                    throw new NoSuchElementException("unknown mapper: " + findType());
                }
                return method.invoke(suchMapper, args);
            }
        });
    }

    protected M findMapper(){
        BeanFactory factory = getFactory();
        return (M) factory.getBean(findType());
    }

    protected Class<? extends GlobalParentMapping> findType(){
        if(type == null){
            Class<? extends AutoMapperController> type = getClass();
            this.type = doFindType0(type);
        }
        return type;
    }

    public static Class<? extends GlobalParentMapping> doFindType0(Class<?> type){
        Class<? extends GlobalParentMapping> result = null;
        for (;;){
            Class<?>[] genericVal = ReflexHandler.superGenericVal(type);
            if (genericVal.length != 1){
                Class<?> superclass = type.getSuperclass();
                if (superclass != null && AutoMapperController.class.isAssignableFrom(superclass)){
                    type = (Class<? extends AutoMapperController>) type.getSuperclass();
                    continue;
                }else {
                    throw new IllegalStateException("error for extends AutoMapperController:" + type);
                }
            }
            result = (Class<? extends GlobalParentMapping>) genericVal[0];
            break;
        }
        return result;
    }

    @Override
    public M getMapper() {
        return mapper;
    }
}
