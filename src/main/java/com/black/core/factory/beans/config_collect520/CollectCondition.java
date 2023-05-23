package com.black.core.factory.beans.config_collect520;

import lombok.Data;

import java.lang.annotation.Annotation;

@SuppressWarnings("all") @Data
public class CollectCondition {

    //目标所携带的注解
    Class<? extends Annotation>[] annotationAt;

    //目标继承的类型
    Class<?>[] type;

    //目标是可实例化的
    boolean soild;

    //自动实例化
    boolean instance;

    //搜索范围
    String[] scope;

    //舍弃无法实例化的
    boolean abandonUnableInstance;

    boolean annotationOr;

    boolean typeOr;

    boolean single = false;

    boolean prototypeCreate;

    boolean sort = true;

    String key;

    Class<? extends ClassMatchCustom>[] customs;
}
