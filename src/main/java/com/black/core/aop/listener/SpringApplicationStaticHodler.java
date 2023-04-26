package com.black.core.aop.listener;

import org.springframework.boot.SpringApplication;

public class SpringApplicationStaticHodler {

    static SpringApplication springApplication;

    public static SpringApplication getSpringApplication(){
        return springApplication;
    }
}
