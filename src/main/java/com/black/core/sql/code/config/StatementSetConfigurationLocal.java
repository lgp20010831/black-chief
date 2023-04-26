package com.black.core.sql.code.config;

import com.black.core.query.MethodWrapper;

public class StatementSetConfigurationLocal {

    private static final ThreadLocal<StatementValueSetDisplayConfiguration> configurationLocal = new ThreadLocal<>();

    public static void registerConfig(StatementValueSetDisplayConfiguration configuration){
        configurationLocal.set(configuration);
    }

    public static void displayConfiguration(MethodWrapper mw){
        StatementValueSetDisplayConfiguration config = StatementSetConfigurationManager.parseConfig(mw);
        configurationLocal.set(config);
    }

    public static StatementValueSetDisplayConfiguration getSetValueConfiguration(){
        return configurationLocal.get();
    }

    public static void remove(){
        configurationLocal.remove();
    }
}
