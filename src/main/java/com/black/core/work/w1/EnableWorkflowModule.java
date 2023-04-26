package com.black.core.work.w1;


import com.black.core.work.w1.cache.CacheTask;
import com.black.core.work.w1.cache.DatabaseCacheKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableWorkflowModule {

    //缓存方法
    Class<? extends CacheTask> value() default DatabaseCacheKey.class;
}
