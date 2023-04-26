package com.black.spring.agency;

import java.lang.annotation.*;

/**
 * @author shkstart
 * @create 2023-04-19 10:07
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RewriteOnAnnotation {

    Class<? extends Annotation>[] value() default {};

}
