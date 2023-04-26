package com.black.core.cache;

import com.black.core.aop.servlet.AopControllerIntercept;

public class AopControllerStaticCache {

    private static AopControllerIntercept controllerIntercept;

    public static void setControllerIntercept(AopControllerIntercept controllerIntercept) {
        AopControllerStaticCache.controllerIntercept = controllerIntercept;
    }

    public static AopControllerIntercept getControllerIntercept() {
        return controllerIntercept;
    }
}
