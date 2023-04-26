package com.black.core.permission;

import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.aop.servlet.RestResponseLocal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class RUPResponseAop {

    @Pointcut("@annotation(com.black.core.permission.RUPServletMethod)")
    public void cut(){}

    @Around("cut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        Class<? extends RestResponse> responseType = configuration.getResponseType();
        Object[] args = joinPoint.getArgs();
        try {
            RestResponseLocal.setType(responseType);
            return joinPoint.proceed(args);
        }finally {
            RestResponseLocal.closeType();
        }
    }

}
