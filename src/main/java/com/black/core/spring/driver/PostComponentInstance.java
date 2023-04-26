package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;

public interface PostComponentInstance extends Driver{

    default Object beforeInstance(Class<?> openComponentClass, ChiefExpansivelyApplication expansivelyApplication){
        return null;
    }

    default Object afterInstance(Class<?> openComponentClass, Object openComponent,
                               ChiefExpansivelyApplication expansivelyApplication){
        return openComponent;
    }
}
