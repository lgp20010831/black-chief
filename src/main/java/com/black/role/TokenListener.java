package com.black.role;

import com.black.core.servlet.TokenExpirationException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public interface TokenListener {

    default void dredgeCrossRequest(HttpServletRequest request, HttpServletResponse response, Object handler){}

    default void dredgeRequest(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response){}

    default void dredgeResourceRequest(ResourceHttpRequestHandler resourceHttpRequestHandler, HttpServletRequest request, HttpServletResponse response){

    }

    default void resloverTokenExpiration(HttpServletRequest request, HttpServletResponse response,
                                         Method method, Class<?> beanType, Object bean, TokenExpirationException e){}

    default void resolverTokenInvaild(HttpServletRequest request, HttpServletResponse response,
                                         Method method, Class<?> beanType, Object bean, TokenInvalidException e){}

    default void resolverNoToken(HttpServletRequest request, HttpServletResponse response,
                                         Method method, Class<?> beanType, Object bean){}

    default void passVerification(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response){}
}
