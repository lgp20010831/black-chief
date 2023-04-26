package com.black.core.work.w2.connect.annotation;

import com.black.core.json.Trust;
import com.black.core.work.w2.connect.WorkflowBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Trust
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkflowAdaptationBuilder {

    Class<? extends WorkflowBuilder> value();

}
