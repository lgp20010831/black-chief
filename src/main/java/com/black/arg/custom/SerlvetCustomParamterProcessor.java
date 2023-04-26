package com.black.arg.custom;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.utils.RequestUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

/**
 * @author shkstart
 * @create 2023-04-18 11:11
 */
public class SerlvetCustomParamterProcessor implements CustomParameterProcessor{

    @Override
    public boolean support(ParameterWrapper pw) {
        Class<?> type = pw.getType();
        return ServletRequest.class.isAssignableFrom(type) ||
                ServletResponse.class.isAssignableFrom(type);
    }

    @Override
    public Object getArg(ParameterWrapper pw, MethodWrapper mw, Map<String, Object> originArgMap, Map<String, Object> env) {
        Class<?> type = pw.getType();
        if (ServletRequest.class.isAssignableFrom(type)){
            return RequestUtils.getAllowNullRequest();
        }else if (ServletResponse.class.isAssignableFrom(type)){
            return RequestUtils.getResponse();
        }
        return null;
    }
}
