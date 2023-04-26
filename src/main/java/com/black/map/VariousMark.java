package com.black.map;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VariousMark {

    //用字符串作为标志
    String value() default "";

    //或者使用 ObtainVariousMark 提供标识
    Class<? extends ObtainVariousMark> markprovider() default ObtainVariousMark.class;

    Class<? extends Annotation> customAnnotationType() default VariousMark.class;
}
