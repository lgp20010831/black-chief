package com.black.core.sql.annotation;
import com.black.core.annotation.Mapper;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Mapper
@ImportPlatform(ImportMapperAndPlatform.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportMapperAndPlatform {

    @AliasFor(annotation = ImportPlatform.class)
    Class<?> value();
}
