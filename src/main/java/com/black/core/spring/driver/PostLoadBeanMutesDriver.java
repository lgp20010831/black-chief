package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;

import java.util.Map;

public interface PostLoadBeanMutesDriver extends Driver {


    void postLoadBeanMutes(Map<Class<?>, Object> loadBeanMutes,
                           Map<String, Object> springMutes,
                           Map<Class<? extends OpenComponent>, Object> springLoadComponentMutes,
                           ChiefExpansivelyApplication pettySpringApplication);

}
