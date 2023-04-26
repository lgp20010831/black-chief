package com.black.core.ill.aop;

@GlobalThrowableHandler
public class CaseIllHandler implements IllHandler {

    @Override
    public boolean support(IllSourceWrapper sourceWrapper) {
        return false;
    }

    @Override
    public boolean intercept(IllSourceWrapper sourceWrapper) {
        return true;
    }
}
