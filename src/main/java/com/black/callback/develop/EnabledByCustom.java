package com.black.callback.develop;

import com.black.pattern.Premise;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-05-17 15:43
 */
@SuppressWarnings("all")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledByCustom {

    Class<? extends Premise> value();
}
