package com.black.core.proxy;

import org.springframework.lang.Nullable;
import java.lang.reflect.Proxy;

public class CheckProxy {

    public static boolean isAopProxy(@Nullable Object object) {
        assert object != null;
        return Proxy.isProxyClass(object.getClass()) || object.getClass().getName().contains("$$");
    }

    public static boolean isJdkDynamicProxy(@Nullable Object object) {
        assert object != null;
        return Proxy.isProxyClass(object.getClass());
    }

    public static boolean isCglibProxy(@Nullable Object object) {
        assert object != null;
        return object.getClass().getName().contains("$$");
    }
}
