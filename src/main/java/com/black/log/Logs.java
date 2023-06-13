package com.black.log;

import com.black.core.util.CurrentLineUtils;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-06-06 10:42
 */
@SuppressWarnings("all")
public class Logs {



    public static void info(String msg, Object... params){
        Method callMethod = CurrentLineUtils.loadMethod(1);
        Class<?> callClass = callMethod.getDeclaringClass();
        info(callClass, callMethod, msg, params);
    }

    public static void info(Class<?> callClass, Method callMethod, String msg, Object... params){
        print(callClass, callMethod, LogLevel.INFO, null, msg, params);
    }

    public static void debug(String msg, Object... params){
        Method callMethod = CurrentLineUtils.loadMethod(1);
        Class<?> callClass = callMethod.getDeclaringClass();
        debug(callClass, callMethod, msg, params);
    }

    public static void debug(Class<?> callClass, Method callMethod, String msg, Object... params){
        print(callClass, callMethod, LogLevel.DEBUG, null, msg, params);
    }

    public static void trace(String msg, Object... params){
        Method callMethod = CurrentLineUtils.loadMethod(1);
        Class<?> callClass = callMethod.getDeclaringClass();
        trace(callClass, callMethod, msg, params);
    }

    public static void trace(Class<?> callClass, Method callMethod, String msg, Object... params){
        print(callClass, callMethod, LogLevel.TRACE, null, msg, params);
    }

    public static void error(String msg, Object... params){
        Method callMethod = CurrentLineUtils.loadMethod(1);
        Class<?> callClass = callMethod.getDeclaringClass();
        error(callClass, callMethod, null, msg, params);
    }

    public static void error(Throwable ex, String msg, Object... params){
        Method callMethod = CurrentLineUtils.loadMethod(1);
        Class<?> callClass = callMethod.getDeclaringClass();
        error(callClass, callMethod, ex, msg, params);
    }

    public static void error(Class<?> callClass, Method callMethod, Throwable ex, String msg, Object... params){
        print(callClass, callMethod, LogLevel.ERROR, ex, msg, params);
    }

    protected static void print(Class<?> callClass, Method callMethod, LogLevel level, Throwable ex,
                                String msg, Object... params){
        SerializeGlobalLogConfiguration configuration = SerializeGlobalLogConfiguration.getInstance();
        LogHandler logHandler = new LogHandler(configuration);
        logHandler.print(callClass, callMethod, level, ex, msg, params);
    }
}
