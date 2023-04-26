package com.black.core.entry;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Map;

public interface ItemArgumentResolver {

    boolean support(ParameterWrapper param, MethodWrapper wrapper, String item);

    Object resolver(ParameterWrapper param, MethodWrapper wrapper, Map<String, Object> source, String item);
}
