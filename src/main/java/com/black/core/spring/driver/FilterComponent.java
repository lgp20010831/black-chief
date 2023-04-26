package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;

public interface FilterComponent extends Driver {

    /** 返回true则丢弃他 */
    boolean filter(Class<?> clazz, ChiefExpansivelyApplication application);
}
