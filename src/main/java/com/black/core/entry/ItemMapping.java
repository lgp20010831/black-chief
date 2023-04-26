package com.black.core.entry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemMapping {

    String value() default "";
    /***
     * 想要实现的效果, 给一个方法取映射条目
     * 例如 query
     *
     * @ItemMapping("query")
     * Object query(String id, JSONObjct body){
     *     mapper.doQuery(id, body);
     * }
     *
     * 那么可以通过条目 query() 来映射到此方法
     * query() -->  参数 id = null, body = null
     * 所以在执行时, 可以附带参数列表: Object[2] args 来表示 query方法的两个参数
     * 默认情况下以 方法名作为条目名
     *
     */
}
