package com.black.graphql.handler;

import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.annotation.ObjectParam;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

public class ObjectParamterHandler implements GraphqlHandler {

    @Override
    public boolean supportParam(ParameterWrapper pw, MethodWrapper mw) {
        return pw.getAnnotationSize() == 0 || pw.hasAnnotation(ObjectParam.class);
    }

    @Override
    public void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object) {
        ObjectParam annotation = pw.getAnnotation(ObjectParam.class);
        String name = annotation == null ? pw.getName() : (StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
        object.getRequest().getRequestParameter().addObjectParameter(name, val);
    }
}
