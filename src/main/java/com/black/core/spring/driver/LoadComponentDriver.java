package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;

import java.util.Collection;

public interface LoadComponentDriver extends Driver{

    void load(Collection<Object> loadCache, ChiefExpansivelyApplication chiefExpansivelyApplication);
}
