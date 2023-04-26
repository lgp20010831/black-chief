package com.black.core.aop.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标注在参数上, 且参数的类型必须是{@link PageObject} 类型
 * 此注解生效的前提, 控制器被 aop 代理管理(类标注{@link GlobalEnhanceRestController})
 * 方法上标注{@link OpenIbatisPage} 或　{@link OpenAutonomyPaging}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriedPageObject {
}
