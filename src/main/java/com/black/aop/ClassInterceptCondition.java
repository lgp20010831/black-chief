package com.black.aop;

import lombok.Data;

import java.lang.annotation.Annotation;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:38
 */
@SuppressWarnings("all") @Data
public class ClassInterceptCondition {

    Class<?>[] type;

    Class<? extends Annotation>[] annAt;

    boolean typeAnd;

    boolean annAnd;

    boolean connectMethodWithAnd;

}
