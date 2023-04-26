package com.black.core.aop.code;

import java.lang.reflect.Method;

public interface AopMatchTargetClazzAndMethodMutesHandler {

    /** 返回 true 表示匹配上了 **/
    boolean matchClazz(Class<?> targetClazz);

    boolean matchMethod(Class<?> targetClazz, Method method);
}
