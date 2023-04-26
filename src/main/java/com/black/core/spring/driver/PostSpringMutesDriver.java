package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;

import java.util.Map;

public interface PostSpringMutesDriver extends Driver {


    void processorMutes(Map<String, Object> singleMutes,
                        Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                        ChiefExpansivelyApplication chiefExpansivelyApplication);

}
