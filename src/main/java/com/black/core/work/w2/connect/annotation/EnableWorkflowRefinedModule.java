package com.black.core.work.w2.connect.annotation;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.work.w2.connect.ConditionResolver;
import com.black.core.work.w2.connect.cache.SqlWritorType;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.condition.DefaultConditionResolver;
import com.black.core.work.w2.connect.node.CountersignatureWorkflowNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableWorkflowRefinedModule {

    Class<? extends WorkflowNode> extendNodeType() default CountersignatureWorkflowNode.class;

    int corePoolSize() default 4;

    Class<? extends ConditionResolver> conditionResolverType() default DefaultConditionResolver.class;

    boolean print() default false;

    boolean asynTracker() default true;

    int trackerPoolSize() default 1;

    SqlWritorType writorType() default SqlWritorType.MAP_SQL;

    //map sql alias is workflow
    Class<? extends DataSourceBuilder> dataSourceIfMapSql() default SpringDataSourceBuilder.class;
}
