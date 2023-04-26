package com.black.core.sql.annotation;

import com.black.api.PageTools;
import com.black.core.util.AliasWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@PageTools
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenSQLAutonomyPaging {

    @AliasWith(target = PageTools.class)
    String pageSize() default "pageSize";

    @AliasWith(target = PageTools.class)
    String pageNum() default "pageNum";

}
