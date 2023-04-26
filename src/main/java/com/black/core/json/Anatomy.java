package com.black.core.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Anatomy {

    /***
     * 为了更深层次的解析
     * {@link JSONConvert#getResponseJson(Object)}
     * 只能解析字段表面,如果字段是个 对象
     * 标注了该注解，则会去解析该对象,如果该对象中含有该对象一直循环,则会被检测
     * 还需要标注属性,解析的深度
     */
    boolean upAnalysis() default false;

    /** 如果向上解析, 则解析到最顶部的 class */
    Class<?> topClass() default Object.class;

}
