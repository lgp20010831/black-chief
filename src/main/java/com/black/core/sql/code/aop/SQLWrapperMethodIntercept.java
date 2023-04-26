package com.black.core.sql.code.aop;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.sqls.BoundStatement;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;

public class SQLWrapperMethodIntercept implements AopTaskIntercepet {

    private final WrapperParser parser;

    public SQLWrapperMethodIntercept() {
        parser = new WrapperParser();
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        MethodWrapper methodWrapper = SQLMethodCache.getWrapper(method);

        Object[] args = hijack.getArgs();
        //找到接口主要参数
        ParameterWrapper wrapper = findBody(methodWrapper);
        if (wrapper != null){
            Object wrapperResult = parser.parseWrapper(methodWrapper, args[wrapper.getIndex()]);
            ParameterWrapper wrapperParam = methodWrapper.getSingleParameterByType(BoundStatement.class);
            if (wrapperParam != null){
                args[wrapperParam.getIndex()] = wrapperResult;
            }
        }
        return hijack.doRelease(args);
    }

    protected ParameterWrapper findBody(MethodWrapper methodWrapper){
        ParameterWrapper parameter = methodWrapper.getSingleParameterByAnnotation(RequestBody.class);
        return parameter;
    }
}
