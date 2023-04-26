package com.black.core.sql.code.config;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.utils.LocalMap;
import lombok.NonNull;

import java.util.function.Function;

public class SyntaxManager {

    private static LocalMap<String, SyntaxRangeConfigurer> rangeConfigurerLocalMap = new LocalMap<>();

    public static void registerSyntax(SyntaxRangeConfigurer configurer){
        rangeConfigurerLocalMap.put(configurer.getAlias(), configurer);
    }

    public static SyntaxRangeConfigurer getRangeConfigurer(String alias){
        return rangeConfigurerLocalMap.get(alias);
    }

    public static void releaseSyntax(String alias){
        rangeConfigurerLocalMap.remove(alias);
    }

    public static void clearSyntax(){
        rangeConfigurerLocalMap.clear();
    }

    public static <T> T localSyntax(Configuration configuration, Function<SyntaxConfigurer, T> function){
        String alias = configuration.getDatasourceAlias();
        SyntaxRangeConfigurer rangeConfigurer = getRangeConfigurer(alias);
        if (rangeConfigurer != null){
            SyntaxConfigurer configurer = rangeConfigurer.getConfigurer();
            return function.apply(configurer);
        }
        return null;
    }

    public static <T> T callSyntax(Configuration configuration, ExecutePacket ep, Function<SyntaxConfigurer, T> function){
        return callSyntax(configuration.getMethodWrapper(), ep.getArgs(), function);
    }

    public static <T> T callSyntax(MethodWrapper mw, @NonNull Object[] args, Function<SyntaxConfigurer, T> function){
        ParameterWrapper parameter = mw.getSingleParameterByType(SyntaxConfigurer.class);
        if (parameter == null){
            return null;
        }
        Object arg = args[parameter.getIndex()];
        if (arg == null){
            return null;
        }
        return callSyntax((SyntaxConfigurer) arg, function);
    }

    public static <T> T callSyntax(SyntaxConfigurer configurer, Function<SyntaxConfigurer, T> function){
        return function.apply(configurer);
    }

}
