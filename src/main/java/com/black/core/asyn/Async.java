package com.black.core.asyn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标注在 spring bean 的方法上
 * 方法必须是没有返回值的
 * 那么一旦调用到该方法,则会以任务形式提交到线程池中
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {


}
