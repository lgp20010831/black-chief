package com.black.core.api.tacitly;

import com.black.core.api.handler.RequestHeadersHandler;
import com.black.core.servlet.TokenAuthenticationComponent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class TacitlyRequestHeaderHandler implements RequestHeadersHandler {

    @Override
    public void obtainRequestHeaders(Class<?> controllerClass, Method method, String alias, Map<String, String> headers) {
        final String content_type = "Content-Type";
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (AnnotationUtils.getAnnotation(parameter, RequestPart.class) != null){
                headers.put(content_type, "multipart/form-data; boundary=FormBoundary");
                break;
            }else if (AnnotationUtils.getAnnotation(parameter, RequestBody.class) != null){
                headers.put(content_type, "application/json");
                break;
            }else {
                headers.put(content_type, "application/x-www-form-urlencoded");
            }
        }
        if (TokenAuthenticationComponent.isEnableToken()){
            headers.put("authorization", "Bearer xxxxx");
        }
    }
}
