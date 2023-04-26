package com.black.core.work.w2.connect;


import com.black.core.util.StringUtils;

public class ConnectStaticFactory {

    public static String buildDefaultRouteAlias(String startAlias, String processorName, String endAlias){
        return StringUtils.linkStr( startAlias, "->", endAlias);
    }

    public static ConnectRouteWraper create(String startAlias, String endAlias,
                                     String processorName){
        return create(startAlias, endAlias, processorName, null);
    }

    public static ConnectRouteWraper create(String startAlias, String endAlias,
                                     String processorName, Condition condition){
        return create(startAlias, endAlias, processorName, condition, buildDefaultRouteAlias(startAlias, processorName, endAlias));
    }

    public static ConnectRouteWraper create(String startAlias, String endAlias,
                                     String processorName, Condition condition, String routeAlias){
        return new ConnectRouteWraper(startAlias, endAlias, processorName, condition, routeAlias);
    }
}
