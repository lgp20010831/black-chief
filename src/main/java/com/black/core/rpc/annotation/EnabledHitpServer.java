package com.black.core.rpc.annotation;

import com.black.nio.code.Configuration;
import com.black.core.rpc.core.NullHook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledHitpServer {

    Class<? extends Consumer<Configuration>> hook() default NullHook.class;

}
