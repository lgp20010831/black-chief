package com.black.core.servlet;


import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ControllerResolver {

    public final String CONTROLLER_PATH;

    public ControllerResolver(String controller_path) {
        CONTROLLER_PATH = "* com.example.springautothymeleaf.SpringAutoThymeleafApplication.*(..)";
    }

}
