package com.black.core.ill;

import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * Global exception collection and handling
 * Support exception matching and multithreading to handle exceptions
 */
public class GlobalThrowableCentralizedHandling {

    private static GlobalThrowableCentralizedHandling instance;

    private final Collection<ThrowableResolverWrapper> resolverWrappers = new HashSet<>();

    public static GlobalThrowableCentralizedHandling getInstance(){
        if (instance == null){
            instance = new GlobalThrowableCentralizedHandling();
        }
        return instance;
    }

    //Core open approach
    public static void resolveThrowable(@NonNull Throwable ex){
        CentralizedExceptionHandling.handlerException(ex);
        for (ThrowableResolverWrapper resolverWrapper : getInstance().getResolverWrappers()) {
             Throwable cause = ex;
             while (cause != null){
                 if (resolverWrapper.supportType(cause.getClass())) {
                     if (resolverWrapper.isAsyn()){
                         Throwable copyCause = cause;
                         AsynGlobalExecutor.execute(() ->{
                             resolverWrapper.invokeResolve(copyCause);
                         });
                     }else {
                         resolverWrapper.invokeResolve(cause);
                     }
                     break;
                 }
                 cause = cause.getCause();
             }
        }
    }

    public Collection<ThrowableResolverWrapper> getResolverWrappers() {
        return resolverWrappers;
    }

    public void registerWrappers(Collection<ThrowableResolverWrapper> wrappers){
        if (wrappers != null){
            resolverWrappers.addAll(wrappers);
        }
    }
}
