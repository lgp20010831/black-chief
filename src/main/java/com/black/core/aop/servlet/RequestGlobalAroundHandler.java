package com.black.core.aop.servlet;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.MethodWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@GlobalAround
public class RequestGlobalAroundHandler implements GlobalAroundResolver {

    private HttpServletRequest servletRequest;

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {

        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null){
            servletRequest =requestAttributes.getRequest();
            if (servletRequest == null){
                return args;
            }
            for (ParameterWrapper parameterWrapper : wrapper.getParameterByAnnotation(Header.class)) {
                Header header = parameterWrapper.getAnnotation(Header.class);
                //join header
                Object headerValue = null;
                String value = servletRequest.getHeader(header.value());
                if (value != null){
                    headerValue = convertValue(value, parameterWrapper.getType());
                }
                args[parameterWrapper.getIndex()] = headerValue;
            }


            for (ParameterWrapper parameterWrapper : wrapper.getParameterByAnnotation(RequestAttribute.class)) {
                RequestAttribute attribute = parameterWrapper.getAnnotation(RequestAttribute.class);
                //join attribute
                Object requestAttribute = servletRequest.getAttribute(attribute.value());
                if (requestAttribute != null){
                    requestAttribute = convertValue(requestAttribute, parameterWrapper.getType());
                }
                args[parameterWrapper.getIndex()] = requestAttribute;
            }
        }
        return args;
    }

    protected Object convertValue(Object value, Class<?> type){
        Class<?> valueClass = value.getClass();
        if (!type.isAssignableFrom(valueClass)){
            TypeHandler typeHandler = TypeConvertCache.initAndGet();
            if (typeHandler != null){
                value = typeHandler.convert(type, value);
            }
        }
        return value;
    }

}
