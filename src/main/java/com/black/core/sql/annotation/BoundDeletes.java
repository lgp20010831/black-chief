package com.black.core.sql.annotation;

import com.black.core.json.Alias;
import com.black.core.sql.code.inter.DatabaseCompanyLevel;
import com.black.core.sql.code.sqls.CompanyLevelAdaptation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BoundDeletes {

    String[] excludeTable() default {};

    Class<? extends DatabaseCompanyLevel> company() default CompanyLevelAdaptation.class;

    @Alias("sqlSequences")
    String[] condition() default {};
}
