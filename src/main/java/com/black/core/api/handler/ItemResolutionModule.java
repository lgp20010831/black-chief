package com.black.core.api.handler;


import com.black.core.api.tacitly.ApiDependencyManger;

import java.util.List;

public interface ItemResolutionModule {

    String name();

    /** 条目的规则, 同级用 , 下级用{}包裹 */
    ItemResolutionModule parse(String item, Class<?> superClass, ApiDependencyManger dependencyManger);

    List<Class<?>> dependencyClasses();

    List<String> dependencyFields();

    //他的下级
    List<ItemResolutionModule> lowLevel();
}
