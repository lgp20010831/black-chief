package com.black.core.netty.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NettyUser
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NettyServer {

    //别名
    String value();

    //占用的端口
    int port() default 9876;

    //ip
    String ip() default "localhost";

}
