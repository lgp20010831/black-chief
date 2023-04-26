package com.black.core.aop.code;

public class InterceptHijackWrapperFactory {

    public InterceptHijackWrapperFactory(){

    }

    public InterceptHijackWrapper createWrapper(AopTaskIntercepet intercepet){
        return new InterceptHijackWrapper(intercepet);
    }
}
