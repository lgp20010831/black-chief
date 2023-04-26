package com.black.core.ill;

import com.black.core.util.CentralizedExceptionHandling;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public class ThrowableResolverWrapper {

    private final ThrowableResolver resolver;

    private final Set<Class<? extends Throwable>> errorTypes = new HashSet<>();

    private final boolean asyn;

    public ThrowableResolverWrapper(ThrowableResolver resolver, Class<? extends Throwable>[] throwableTypes, boolean asyn) {
        this.resolver = resolver;
        this.asyn = asyn;
        if (throwableTypes != null){
            for (Class<? extends Throwable> throwableType : throwableTypes) {
                if (!hasSupperType(throwableType)) {
                    errorTypes.add(throwableType);
                }
            }
        }
    }

    public boolean supportType(@NonNull Class<? extends Throwable> type){
        return hasSupperType(type);
    }

    protected boolean hasSupperType(@NonNull Class<? extends Throwable> type){
        for (Class<? extends Throwable> errorType : errorTypes) {
            if (errorType.isAssignableFrom(type)){
                return true;
            }
        }
        return false;
    }

    public boolean isAsyn() {
        return asyn;
    }

    public void invokeResolve(Throwable ex){
        try {
            resolver.doResolve(ex);
        } catch (Throwable e) {
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}
