package com.black.core.aop.servlet.plus.parse;

import com.black.core.aop.servlet.plus.EntryWrapper;
import com.black.core.aop.servlet.plus.PlusMethodWrapper;
import com.black.core.aop.servlet.plus.WriedEntity;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@PlusArgumentParser
public class WriedEntryHandler implements ArgumentParser {

    @Override
    public boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper) {
        return methodWrapper.getAnnotationMap().containsKey(WriedEntity.class);
    }

    @Override
    public Object[] parseArgument(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper, Object[] args) {
        Method method = methodWrapper.getMethod();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            WriedEntity wriedEntity = AnnotationUtils.getAnnotation(parameter, WriedEntity.class);
            if (wriedEntity != null){
                Class<?> targetClazz = entryWrapper.getTargetClazz();
                if (parameter.getType().equals(targetClazz)){
                    args[i] = entryWrapper.getSource();
                }else {
                    throw new RuntimeException("wried Entry 对象必须是实体类对象");
                }
            }
        }
        return args;
    }

}
