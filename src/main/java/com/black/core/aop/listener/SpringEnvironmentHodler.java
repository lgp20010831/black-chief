package com.black.core.aop.listener;

import org.springframework.core.env.ConfigurableEnvironment;

public class SpringEnvironmentHodler {

    static ConfigurableEnvironment environment;

    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }
}
