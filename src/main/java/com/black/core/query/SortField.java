package com.black.core.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)      //作用到属性上的注解
@Retention(RetentionPolicy.RUNTIME)  //在运行期间可被检测
public @interface SortField {
}
