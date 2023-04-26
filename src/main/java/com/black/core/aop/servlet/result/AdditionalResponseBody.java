package com.black.core.aop.servlet.result;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalResponseBody {

    Class<? extends ResponseBodyHandler>[] value() default HumpBodyHandler.class;

    //false 则实例化通过 instanceFactory
    boolean instanceByBeanFactory() default true;

}
