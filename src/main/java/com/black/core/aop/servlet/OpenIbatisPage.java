package com.black.core.aop.servlet;

import com.black.api.PageTools;
import com.black.core.util.AliasWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 开启分页注解
 * 标注在接口方法上,
 * 前提类上需要标注{@link GlobalEnhanceRestController}注解,让 aop 代理控制器
 * 标注注解后, 会自动解析参数, 根据参数中是否存在 pagesize, pagenum 来进行分页
 * 参数名可以进行设定
 * 当 priority 为 true 时, 注解属性 pageSize 和 pageNum 声明的参数名才会生效
 * 否则按照 {@link AopControllerIntercept#pageNumName} 和 {@link AopControllerIntercept#pageSizeName} 进行生效
 */
@PageTools
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenIbatisPage {

    @AliasWith(target = PageTools.class)
    String pageSize() default "pageSize";

    //分页属性名
    @AliasWith(target = PageTools.class)
    String pageNum() default "pageNum";

    //属性优先级
    boolean priority() default false;
}
