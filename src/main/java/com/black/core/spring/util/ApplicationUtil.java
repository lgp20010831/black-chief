package com.black.core.spring.util;

import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;
import com.black.core.sql.code.log.Log;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

@Log4j2
public final class ApplicationUtil {

    private static final ObjectSerializer objectSerializer = new IoSerializer();
    public static String RUN_TIME__MILLS_MESSAGE = "程序执行时间: ===>";
    public static final String DEFAULT_TASK = "[task]";
    public static <E> Collection<E> clone(Collection<E> target){
        if (target != null){
            return new ArrayList<>(target);
        }
        return null;
    }

    public static <E> E deepClone(E target){
        if (target != null){
            return objectSerializer.copyObject(target);
        }
        return null;
    }

    public static boolean isInterOrAbstractOrEnum(Class<?> clazz){
        if (clazz != null){
            return clazz.isInterface() || clazz.isEnum() || Modifier.isAbstract(clazz.getModifiers());
        }
        return true;
    }

    public static void programRunMills(Runnable runnable, String name){
        long startTime = System.currentTimeMillis();
        runnable.run();
        if (log.isInfoEnabled()) {
            log.info("{} {} {} 毫秒", name, RUN_TIME__MILLS_MESSAGE,
                    System.currentTimeMillis() - startTime);
        }
    }

    public static void programRunMills(Runnable runnable, String name, Log log){
        programRunMills(runnable, name, log, "~~~>>");
    }

    public static void programRunMills(Runnable runnable, String name, Log log, String prefix){
        long startTime = System.currentTimeMillis();
        runnable.run();
        if (log.isInfoEnabled()) {
            log.info(StringUtils.linkStr(prefix, name, " ", RUN_TIME__MILLS_MESSAGE, " ",
                    String.valueOf(System.currentTimeMillis() - startTime), " 毫秒"));
        }
    }

    public static <V> V programRunMills(Callable<V> callable, String name, Log log, String prefix){
        V result;
        long startTime = System.currentTimeMillis();
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (log.isInfoEnabled()) {
            log.info(StringUtils.linkStr(prefix, name, " ", RUN_TIME__MILLS_MESSAGE, " ",
                    String.valueOf(System.currentTimeMillis() - startTime), " 毫秒"));
        }
        return result;
    }

    public static void programRunMills(Runnable runnable){
        programRunMills(runnable, DEFAULT_TASK);
    }

    public static void programRunNano(Runnable runnable){
        programRunNano(runnable, DEFAULT_TASK);
    }

    public static void programRunNano(Runnable runnable, String name){
        long startTime = System.nanoTime();
        runnable.run();
        if (log.isInfoEnabled()) {
            log.info("{} {} {} 纳秒", name, RUN_TIME__MILLS_MESSAGE, System.nanoTime() - startTime);
        }
    }



    public static <V> V programRunMills(Callable<V> callable) {
        return programRunMills(callable, DEFAULT_TASK);
    }

    public static <V> V programRunMills(Callable<V> callable, String name) {
        long startTime = System.currentTimeMillis();
        V call;
        try {
            call = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (log.isInfoEnabled()) {
            log.info("{} {} {} 毫秒", name, RUN_TIME__MILLS_MESSAGE, System.currentTimeMillis() - startTime);
        }
        return call;
    }

    public static <V> V programRunNano(Callable<V> callable) {
        return programRunNano(callable, DEFAULT_TASK);
    }

    public static <V> V programRunNano(Callable<V> callable, String name) {
        long startTime = System.nanoTime();
        V call;
        try {
            call = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (log.isInfoEnabled()) {
            log.info("{} {} {} 纳秒", name, RUN_TIME__MILLS_MESSAGE, System.nanoTime() - startTime);
        }
        return call;
    }
}
