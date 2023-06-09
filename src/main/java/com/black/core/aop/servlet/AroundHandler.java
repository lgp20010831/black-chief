package com.black.core.aop.servlet;


import com.black.core.query.MethodWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

@GlobalAround @Log4j2
public class AroundHandler implements GlobalAroundResolver{

    private final ThreadLocal<MethodParser> parserThreadLocal = new ThreadLocal<>();

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        Class<?> controllerClazz = httpMethodWrapper.getControllerClazz();
        if (wrapper.hasAnnotation(AnalyzedMethod.class) || AnnotationUtils.getAnnotation(controllerClazz, AnalyzedMethod.class) != null){
            AnalyzedMethod analyzedMethod = wrapper.getAnnotation(AnalyzedMethod.class);
            MethodParser parser = parserThreadLocal.get();
            if (parser == null){
                parser = new MethodParser();
                parserThreadLocal.set(parser);
            }
            String alias = null;
            if (analyzedMethod != null){
                alias = analyzedMethod.value();
            }else {
                if (wrapper.parameterHasAnnotation(AnalyzedBody.class)) {
                    alias = wrapper.getSingleParameterByAnnotation(AnalyzedBody.class).getName();
                }
            }

            try {
                return parser.parse(StringUtils.hasText(alias) ? alias : null, wrapper.getMethod(), args);
            }catch (Throwable e){
                log.debug("parse AnalyzedMethod args fair: {}", e.getMessage());
                return args;
            }
        }
        return args;
    }
}
