package com.black.test;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Map;

public interface ApiInfoGetter {

    String[] getRequestUrls(MethodWrapper mw, ClassWrapper<?> cw);

    Map<String, String> getHeaderMap(MethodWrapper mw, ClassWrapper<?> cw);

    String[] getRequestMethods(MethodWrapper mw, ClassWrapper<?> cw);

    Object getRequestExample(MethodWrapper mw, ClassWrapper<?> cw);
}
