package com.black.core.sql.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoveBeforeInsert {

    //书写表达式
    String blendVoice() default "";

    //书写删除语句时新增的序列
    @Alias("sqlSequences")
    String[] removeSequence() default {};
}
