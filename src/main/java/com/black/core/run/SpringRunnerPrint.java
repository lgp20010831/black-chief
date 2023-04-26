package com.black.core.run;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class SpringRunnerPrint implements SpringApplicationRunListener {

    public static boolean print = true;

    public static SpringApplication application;

    public static String[] args;

    public SpringRunnerPrint(SpringApplication springApplication, String[] args){
        SpringRunnerPrint.application = springApplication;
        SpringRunnerPrint.args = args;
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    public static void closePrint(){
        print = false;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application starting"));
        }
        SpringApplicationRunListener.super.starting(bootstrapContext);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application context prepared"));
        }
        SpringApplicationRunListener.super.contextPrepared(context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application context loaded"));
        }
        SpringApplicationRunListener.super.contextLoaded(context);
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application started"));
        }
        SpringApplicationRunListener.super.started(context);
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application running"));
        }
        SpringApplicationRunListener.super.running(context);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application fail"));
        }
        SpringApplicationRunListener.super.failed(context, exception);
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        if (print){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "spring application environment prepared"));
        }
        SpringApplicationRunListener.super.environmentPrepared(bootstrapContext, environment);
    }
}
