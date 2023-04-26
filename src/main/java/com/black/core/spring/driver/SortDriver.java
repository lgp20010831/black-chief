package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;

import java.util.Collection;
import java.util.Map;

public interface SortDriver extends Driver {

     void sort(Collection<Object> loadCache, Map<Class<? extends OpenComponent>, Object> springLoadComponent,
               ChiefExpansivelyApplication chiefExpansivelyApplication);
}
