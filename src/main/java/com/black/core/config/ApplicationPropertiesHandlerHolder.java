package com.black.core.config;

public class ApplicationPropertiesHandlerHolder {

    private static ApplicationPropertiesHandler handler;

    public static ApplicationPropertiesHandler getHandler() {
        if (handler == null) handler = new ApplicationPropertiesHandler();
        return handler;
    }
}
