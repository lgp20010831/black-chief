package com.black.core.sql.run;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.RunScript;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.InheritGlobalConvertHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunSqlHandler implements RunSupport{

    private final Map<Method, RunConfiguration> configCache = new ConcurrentHashMap<>();

    private final RunSqlProcessor processor = new RunSqlProcessor();

    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(RunScript.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        RunScript annotation = mw.getAnnotation(RunScript.class);
        RunConfiguration runConfiguration = configCache.computeIfAbsent(mw.getMethod(), method -> {
            return AnnotationUtils.loadAttribute(annotation, new RunConfiguration());
        });
        AliasColumnConvertHandler handler = runConfiguration.getHandler();
        if (handler instanceof InheritGlobalConvertHandler){
            runConfiguration.setHandler(configuration.getConvertHandler());
        }
        return processor.invoke(configuration, runConfiguration, args, mw);
    }
}
