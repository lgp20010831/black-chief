package com.black.core.aop.servlet;

import com.black.api.PageTools;
import com.black.core.util.AliasWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 表示自主分页
 * 不同于注解{@link OpenIbatisPage}
 * 本注解不会主动解析参数来进行分页, 而是需要配合{@link WriedPageObject} 注入
 * {@link PageObject} 对象, 来进行分页
 * 同样需要被 aop 代理控制器
 */
@PageTools
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenAutonomyPaging {

    @AliasWith(target = PageTools.class)
    String pageSize() default "pageSize";

    //分页属性名
    @AliasWith(target = PageTools.class)
    String pageNum() default "pageNum";

    //属性优先级
    boolean priority() default false;

}
