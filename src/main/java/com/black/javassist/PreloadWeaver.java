package com.black.javassist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("all")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreloadWeaver {

}
