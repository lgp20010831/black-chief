package com.black.core.aop.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//将此注解标注在方法上
//那么就回去解析该方法, 将 JSON 参数解析出来注入
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnalyzedMethod {


    //要解析的 json 参数
    //不指定则默认为 requestBody 修饰的参数
    String value() default "";


}
