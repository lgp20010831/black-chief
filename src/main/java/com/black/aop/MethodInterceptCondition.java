package com.black.aop;

import lombok.Data;

import java.lang.annotation.Annotation;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:38
 */
@SuppressWarnings("all") @Data
public class MethodInterceptCondition {

    //需要指定方法上带有指定的注解
    Class<? extends Annotation>[] annAt;

    //需要支持方法某个参数中带有指定的注解
    Class<? extends Annotation>[] paramAt;

    //只支持方法的返回值类型数组
    Class<?>[] supportReturnType;

    //是否要求方法必须公开
    boolean openMethod;

    boolean annAnd;

    String[] methodNames;

}
