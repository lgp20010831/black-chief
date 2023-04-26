package com.black.core.aop.servlet;

import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@GlobalAround
public class ThrowableAroundHandler implements GlobalAroundResolver{

    private final Map<Class<?>, ThrowableAvoidConfiguration> avoidConfigurations = new ConcurrentHashMap<>();

    @Override
    public Object handlerException(Throwable e, Class<? extends RestResponse> responseClass, HttpMethodWrapper httpMethodWrapper) throws Throwable {

        Class<?> primordialClass = BeanUtil.getPrimordialClass(httpMethodWrapper.getControllerClazz());
        AvoidThrowable avoidThrowable = AnnotationUtils.getAnnotation(primordialClass, AvoidThrowable.class);
        if (avoidThrowable != null){
            ThrowableAvoidConfiguration configuration = avoidConfigurations.computeIfAbsent(primordialClass, pc -> new ThrowableAvoidConfiguration(pc, avoidThrowable));
            Throwable cause = e;
            Set<Class<? extends Throwable>> errorAvoidSources = configuration.getErrorAvoidSources();
            if (!errorAvoidSources.isEmpty()){
                while (cause != null){
                    for (Class<? extends Throwable> errorAvoidSource : errorAvoidSources) {
                        if (errorAvoidSource.isAssignableFrom(cause.getClass())){
                            //成功规避
                            throw new RuntimeException(configuration.getMessage(), e);
                        }
                    }
                    cause = cause.getCause();
                }
            }
        }
        throw e;
    }
}
