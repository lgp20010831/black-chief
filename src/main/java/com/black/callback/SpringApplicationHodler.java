package com.black.callback;

import com.black.core.spring.ChooseScanRangeHolder;
import com.black.core.util.ClassUtils;
import org.springframework.boot.SpringApplication;

import java.util.HashSet;
import java.util.Set;

public class SpringApplicationHodler {

    private static SpringApplication springApplication;

    //启动参数
    private static String[] args;

    public static void setArgs(String[] args) {
        SpringApplicationHodler.args = args;
    }

    public static String[] getArgs() {
        return args;
    }

    public static void setSpringApplication(SpringApplication springApplication) {
        SpringApplicationHodler.springApplication = springApplication;
    }

    public static SpringApplication getSpringApplication() {
        return springApplication;
    }

    public static String[] getSpringProjectRange(){
        SpringApplication application = SpringApplicationHodler.getSpringApplication();
        if (application == null){
            return new String[0];
        }
        Set<Object> allSources = application.getAllSources();
        Set<String> scanPackages = new HashSet<>();
        for (Object allSource : allSources) {
            if (allSource instanceof Class<?>){
                scanPackages.add(ClassUtils.getPackageName((Class<?>) allSource));
            }
        }

        new ChooseScanRangeHolder().screenRange(scanPackages.toArray(new String[0]));
        return ChooseScanRangeHolder.obtainRanges();
    }
}
