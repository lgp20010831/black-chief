package com.black.fun_net;

import java.lang.annotation.*;

@SuppressWarnings("all")
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Annotations {

    Class<? extends Annotation>[] value() default {};
}
