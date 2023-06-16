package com.black.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.adapter.AdvisorAdapter;

/**
 * @author 李桂鹏
 * @create 2023-06-16 14:00
 */
@SuppressWarnings("all")
public class BlackAopAdvisorAdapter implements AdvisorAdapter {

    @Override
    public boolean supportsAdvice(Advice advice) {
        return false;
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        return (MethodInterceptor) advisor.getAdvice();
    }
}
