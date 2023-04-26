package com.black.core.mybatis.intercept.annotation;


import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicallyIbtaisIntercept {

    @Alias("methodAliases")
    String[] value();

    /** 默认为所有不同数据源的 config 做拦截器 */
    String alias() default "";
}
