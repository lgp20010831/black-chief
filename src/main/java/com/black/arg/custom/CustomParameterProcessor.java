package com.black.arg.custom;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Map;

/**
 * @author shkstart
 * @create 2023-04-18 11:06
 */
public interface CustomParameterProcessor {

    boolean support(ParameterWrapper pw);

    Object getArg(ParameterWrapper pw, MethodWrapper mw, Map<String, Object> originArgMap, Map<String, Object> env);
}
