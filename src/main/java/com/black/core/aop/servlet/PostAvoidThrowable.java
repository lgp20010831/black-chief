package com.black.core.aop.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//作用在方法上, 作用域为当前类
//当成功规避异常后, 执行标注的方法
//方法的参数, 会通过 instanceFactory 注入
//方法如果存在返回值, 则会注入到响应类的 result 里
//同一个类里标注此注解的方法只能有一个
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostAvoidThrowable {




}
