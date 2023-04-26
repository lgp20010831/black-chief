package com.black.graphql.handler;

import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.annotation.Param;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

import java.util.Map;

public class ParamterHandler implements GraphqlHandler {


    @Override
    public boolean supportParam(ParameterWrapper pw, MethodWrapper mw) {
        return pw.getAnnotationSize() == 0 || pw.hasAnnotation(Param.class);
    }


    @Override
    public void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object) {
        Param annotation = pw.getAnnotation(Param.class);
        if (val instanceof Map){
            Map<String, Object> map = (Map<String, Object>) val;
            map.forEach((k, v) ->{
                object.getRequest().addParameter(k, v);
            });
        }else {
            String name = annotation == null ? pw.getName() : (StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
            object.getRequest().addParameter(name, val);
        }

    }
}
