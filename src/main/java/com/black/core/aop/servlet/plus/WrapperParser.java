package com.black.core.aop.servlet.plus;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public interface WrapperParser {


    Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper,
                     List<?> listArg,
                     Class<?> genericType,
                     Class<?> entity,
                     Annotation wrapperAnnotation,
                     PlusMethodWrapper methodWrapper, boolean parsed);


    Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper,
                     Map<String, Object> mapArg,
                     Class<?> entity,
                     Annotation wrapperAnnotation,
                     PlusMethodWrapper methodWrapper);

    Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper,
                     Object instance, Class<?> entity,
                     Annotation wrapperAnnotation,
                     PlusMethodWrapper methodWrapper);

}
