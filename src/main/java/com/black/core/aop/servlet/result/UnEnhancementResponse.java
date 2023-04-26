package com.black.core.aop.servlet.result;

import com.black.core.aop.servlet.UnEnhancementRequired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@UnEnhancementRequired
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnEnhancementResponse {
}
