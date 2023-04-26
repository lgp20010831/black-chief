package com.black.core.aop.code;

public class InterceptHijackWrapper {

    AopTaskIntercepet intercepet;

    public void setIntercepet(AopTaskIntercepet intercepet) {
        this.intercepet = intercepet;
    }

    public AopTaskIntercepet getIntercepet() {
        return intercepet;
    }

    public InterceptHijackWrapper(AopTaskIntercepet intercepet) {
        this.intercepet = intercepet;
    }
}
