package com.black.core.mark;

import com.black.core.query.MethodWrapper;

public interface MarksHandler {

    default Object[] parseParams(MethodWrapper wrapper, Object[] args){
        return args;
    }

    default boolean intercept(MethodWrapper wrapper, Object[] args){
        return false;
    }

    default Object[] beforeRelease(MethodWrapper wrapper, Object[] args){
        return args;
    }

    default Object afterRelease(MethodWrapper wrapper, Object result){
        return result;
    }

    default Object processorThrowable(MethodWrapper wrapper, Throwable ex, Object captureResult) throws Throwable{
        throw ex;
    }

    default Object interceptCallBack(MethodWrapper wrapper, Object[] args, Object chainResult){
        return chainResult;
    }
}
