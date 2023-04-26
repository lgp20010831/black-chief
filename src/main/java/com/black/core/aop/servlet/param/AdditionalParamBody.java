package com.black.core.aop.servlet.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalParamBody {

    Class<? extends ParamBodyHandler> value() default RuacnlParamBodyHandler.class;

}
