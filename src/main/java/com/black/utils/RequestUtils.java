package com.black.utils;

import com.black.core.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestUtils {

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(requestAttributes, "non null of ServletRequestAttributes");
        HttpServletRequest request = requestAttributes.getRequest();
        Assert.notNull(requestAttributes, "non null of HttpServletRequest");
        return request;
    }

    public static HttpServletRequest getAllowNullRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return null;
        }
        return requestAttributes.getRequest();
    }

    public static HttpServletResponse getResponse(){
        ServletWebRequest servletContainer = (ServletWebRequest)RequestContextHolder.getRequestAttributes();
        return servletContainer.getResponse();
    }

    public static String getHeader(String txt){
        HttpServletRequest request = getRequest();
        return request.getHeader(txt);
    }

    public static Object getAttribute(String txt){
        HttpServletRequest request = getRequest();
        return request.getAttribute(txt);
    }

    public static String getParamter(String txt){
        return getRequest().getParameter(txt);
    }
}
