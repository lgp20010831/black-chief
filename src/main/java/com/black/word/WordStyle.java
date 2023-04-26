package com.black.word;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WordStyle {

    int fontSize() default 14;

    boolean bold() default false;

    String color() default "#ffffff";

    String fontFamily() default "微软雅黑";

    //删除线
    boolean strike() default false;

    //背景高亮色
    boolean underLine() default false;

    //下划线
    UnderlinePatterns underlinePatterns() default UnderlinePatterns.NONE;

    //斜体
    boolean italic() default false;
}
