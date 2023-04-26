package com.black.core.util;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasWith {

    String name() default "";

    Class<? extends Annotation> target() default Annotation.class;
}
