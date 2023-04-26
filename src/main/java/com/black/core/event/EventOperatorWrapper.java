package com.black.core.event;

import com.black.core.cache.EntryCache;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.entry.EntryExtenderDispatcher;
import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

//实际是对监听者们的封装
@Getter
@SuppressWarnings("all")
public class EventOperatorWrapper {

    //监听者方法, 每一个方法对应一个封装
    private final Method method;
    private final Object obj;
    private boolean asyn;
    private final ReadEvent readEvent;
    private Class<?> primordialClass;
    public EventOperatorWrapper(Method method, Object obj, ReadEvent readEvent) {
        this.method = method;
        this.obj = obj;
        primordialClass = BeanUtil.getPrimordialClass(obj);
        this.readEvent = readEvent;
        asyn = readEvent.asyn();
    }

    public Object invoke(Object event, String entry) throws Throwable{
        int count = method.getParameterCount();
        if (count == 0){
            return method.invoke(obj, new Object[0]);
        }
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (count == 1){
            if (event == null){
                return method.invoke(obj, new Object[1]);
            }
            //拿到第一个参数
            Class<?> primordialType = BeanUtil.getPrimordialClass(event);
            Class<?> parameterType = method.getParameterTypes()[0];
            if (!parameterType.isAssignableFrom(primordialType)){
                event = typeHandler.convert(parameterType, event);
            }
            return method.invoke(obj, new Object[]{event});
        }

        //处理多参数
        Object[] finallyArgs = new Object[count];
        Parameter[] parameters = method.getParameters();
        PrimaryParam primaryParam = null;
        int primaryIndex = -1;
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = parameters[i];

            //找出主参数
            PrimaryParam param = AnnotationUtils.getAnnotation(parameter, PrimaryParam.class);
            if (param != null){
                if (primaryParam != null){
                    throw new RuntimeException("只能有一个参数标注 " + method);
                }
                primaryParam = param;
                primaryIndex = i;
            }

            TypeHandler handler = TypeConvertCache.initAndGet();
            SetEntry setEntry = AnnotationUtils.getAnnotation(parameter, SetEntry.class);
            if (setEntry != null){
                Class<?> type = parameter.getType();
                Object val = entry;
                if (!type.equals(String.class)){
                    val = handler.convert(type, val);
                }
                finallyArgs[i] = val;
            }else {

                if (primaryIndex == i){
                    finallyArgs[i] = handlerArg(event, event, parameter, primaryParam);
                }else {
                    finallyArgs[i] = handlerArg(event, null, parameter, primaryParam);
                }
            }
        }
        return method.invoke(obj, finallyArgs);
    }


    private Object handlerArg(Object event, Object arg, Parameter parameter, PrimaryParam primaryParam){
        ItemParam itemParam = AnnotationUtils.getAnnotation(parameter, ItemParam.class);
        if (itemParam != null){
            //准备一个数据源
            Map<String, Object> source = obtainSource(event, primaryParam);
            EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();
            return dispatcher.handlerByMap(itemParam.value(), source);

        }
        if (arg == null){
            InstanceFactory instanceFactory = ChiefApplicationHolder.getExpansivelyApplication().instanceFactory();
            Object instance = instanceFactory.getInstance(parameter.getType());
            return instance;
        }
        return arg;
    }

    private Map<String, Object> obtainSource(Object event, PrimaryParam primaryParam){
        if (event == null){
            return null;
        }

        if(event instanceof Map){
            return (Map<String, Object>) event;
        }
        EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();
        if (primaryParam != null){
            String value = primaryParam.value();
            if (!StringUtils.hasText(value)){
                return null;
            }
        }
        return null;
    }
}
