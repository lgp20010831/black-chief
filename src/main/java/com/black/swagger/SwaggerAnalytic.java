package com.black.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerAnalytic {

    /*
        请求表达式
        1. 支持自定义 json 语法   {key:val ...} [{key:xxxx}]
        2. 支持 url 自定义参数写法  ?xxx=xxx ...
        3. 支持 实体类 blend 嵌套语法 user{}  ->  {xxx:xxx}
        4. 支持 + 拼接语法  {key:val} + ?xxx=xxx
     */
    String request() default "";

}
