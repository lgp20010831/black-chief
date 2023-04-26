package com.black.core.annotation;



import com.black.core.api.annotation.EnableApiCollector;
import com.black.core.template.EnableTxtTemplateHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableApiCollector
@EnableTxtTemplateHolder
public @interface EnableGlobalTxtUnionApiUnionOperatorCode {


}
