package com.black.core.sql.code.config;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.StatementValueSetConfiguration;
import com.black.core.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatementSetConfigurationManager {

    private static Map<Method, StatementValueSetDisplayConfiguration> configurationCache = new ConcurrentHashMap<>();

    public static StatementValueSetDisplayConfiguration parseConfig(MethodWrapper mw){
        Method method = mw.get();
        return configurationCache.computeIfAbsent(method, m -> {
            StatementValueSetConfiguration annotation = mw.getAnnotation(StatementValueSetConfiguration.class);
            StatementValueSetDisplayConfiguration configuration = new StatementValueSetDisplayConfiguration();
            if (annotation != null){
                AnnotationUtils.loadAttribute(annotation, configuration);
            }
            return configuration;
        });
    }
}
