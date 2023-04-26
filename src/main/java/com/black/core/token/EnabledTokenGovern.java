package com.black.core.token;

import com.black.role.ConfigurationBiko;
import com.black.role.TokenPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledTokenGovern {

    Class<? extends ConfigurationBiko> biko() default ConfigurationBiko.class;

    TokenPattern pattern() default TokenPattern.DEFAULT;

}
