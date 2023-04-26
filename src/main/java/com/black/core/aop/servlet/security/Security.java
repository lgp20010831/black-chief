package com.black.core.aop.servlet.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Security {

    Class<? extends SecurityHandler> handler() default UrlSecutityHandler.class;

    Class<? extends SecurityResponseHandler> response() default DefaultSecurityResponse.class;
}
