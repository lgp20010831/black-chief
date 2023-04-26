package com.black.core.http.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Autowired @Lazy
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenHttpMapper {



}
