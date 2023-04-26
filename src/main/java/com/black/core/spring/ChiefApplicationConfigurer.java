package com.black.core.spring;

import java.util.Collection;

public interface ChiefApplicationConfigurer {

    default String[] scanPackages(){
        return null;
    }

    default boolean cancelLoad(){
        return false;
    }

    default boolean addSpringScanPackages(){return true;}

    default Class<?> pointSpringBootStartUpClazz(){
        return null;
    }

    default boolean printLog(){
        return true;
    }

    default Collection<Class<?>> registerComponentMutes(){
        return null;
    }

}
