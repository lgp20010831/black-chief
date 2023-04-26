package com.black.core.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 此注解是一个标记注解
 * 通常用于标记可以解析参数的实体类
 * 在进行 json 转换时或者其他时候, 被识别
 * 表示可以信任的对象, 字段, 方法
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Trust {

}
