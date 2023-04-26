package com.black.core.aop.listener;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;

public class GlobalAopRunnerListener implements SpringApplicationRunListener {

    private static SpringApplication springApplication;
    private static String[] args;

    public GlobalAopRunnerListener(SpringApplication springApplication, String[] args) {
        GlobalAopRunnerListener.springApplication = springApplication;
        GlobalAopRunnerListener.args = args;
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        SpringEnvironmentHodler.environment = environment;
    }

    public static SpringApplication getSpringApplication() {
        return springApplication;
    }

    public static String[] getArgs() {
        return args;
    }
}
