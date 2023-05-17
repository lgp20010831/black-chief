package com.black.callback.develop;

import java.lang.annotation.*;

/**
 * @author 李桂鹏
 * @create 2023-05-17 15:40
 */
@SuppressWarnings("all")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledByAnnotation {

    Class<? extends Annotation> value();
}
