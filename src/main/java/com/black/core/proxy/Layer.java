package com.black.core.proxy;

import com.black.core.json.Alias;
import com.black.utils.Null;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Layer {

    @Alias("proxy")
    Class<?>[] value() default Null.class;

    String[] scannerPackages() default {};

    boolean lazyForSpring() default true;
}
