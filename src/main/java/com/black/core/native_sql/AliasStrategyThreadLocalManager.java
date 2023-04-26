package com.black.core.native_sql;

import com.black.core.sql.code.AliasColumnConvertHandler;

public class AliasStrategyThreadLocalManager {

    private static final ThreadLocal<AliasColumnConvertHandler> converHandlerLocal = new ThreadLocal<>();

    public static ThreadLocal<AliasColumnConvertHandler> getConverHandlerLocal() {
        return converHandlerLocal;
    }

    public static AliasColumnConvertHandler getConvertHandler(){
        return converHandlerLocal.get();
    }

    public static void remove(){
        converHandlerLocal.remove();
    }

    public static void set(AliasColumnConvertHandler convertHandler){
        converHandlerLocal.set(convertHandler);
    }
}
