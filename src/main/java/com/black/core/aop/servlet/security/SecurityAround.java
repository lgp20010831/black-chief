package com.black.core.aop.servlet.security;

import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;

//@GlobalAround
//目的: 实现拦截器层面的拦截
//首先应该定义安全的范围
//哪个控制器, 哪个方法, 如果方法拦蓄嵌套怎么处理
//拦截的定义, 比如 url, 参数, 方法自定义实现
//拦截成功后的返回值
public class SecurityAround implements GlobalAroundResolver {

    final InstanceFactory factory;

    public SecurityAround(InstanceFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean intercept(Object[] args, HttpMethodWrapper mw) {
        SecurityHandler securityHandler = getSecurityHandler(mw);
        if (securityHandler == null){
            return false;
        }
        return securityHandler.doIntercept(args, mw);
    }

    @Override
    public Object interceptCallBack(HijackObject hijack, Object[] args, HttpMethodWrapper mw, Object chainResult) {
        SecurityResponseHandler responseHandler = getSecurityResponse(mw);
        Assert.notNull(responseHandler, "not find response handler");
        return responseHandler.getResponse();
    }

    private SecurityHandler getSecurityHandler(HttpMethodWrapper mw){
        Security annotation = mw.getMethodWrapper().getAnnotation(Security.class);
        if (annotation == null){
            annotation = mw.getMethodWrapper().getDeclaringClassWrapper().getAnnotation(Security.class);
        }
        if (annotation == null){
            return null;
        }
        Class<? extends SecurityHandler> handler = annotation.handler();
        if (!BeanUtil.isSolidClass(handler)){
            throw new IllegalStateException("security handler is not a solid class");
        }
        return factory.getInstance(handler);
    }

    private SecurityResponseHandler getSecurityResponse(HttpMethodWrapper mw){
        Security annotation = mw.getMethodWrapper().getAnnotation(Security.class);
        if (annotation == null){
            annotation = mw.getMethodWrapper().getDeclaringClassWrapper().getAnnotation(Security.class);
        }
        if (annotation == null){
            return null;
        }
        Class<? extends SecurityResponseHandler> handler = annotation.response();
        if (!BeanUtil.isSolidClass(handler)){
            throw new IllegalStateException("security response handler is not a solid class");
        }
        return factory.getInstance(handler);
    }
}
