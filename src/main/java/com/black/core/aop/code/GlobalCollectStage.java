package com.black.core.aop.code;

import java.util.Set;

public interface GlobalCollectStage {

    /** 收集所有有资格的 class 对象 */
    Set<Class<?>> collectQualifiedClassMutes();
}
