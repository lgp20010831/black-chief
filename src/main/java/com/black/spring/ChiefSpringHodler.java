package com.black.spring;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class ChiefSpringHodler {

    private static ConfigurableApplicationContext applicationContext;

    private static DefaultListableBeanFactory chiefAgencyListableBeanFactory;


    public static DefaultListableBeanFactory getChiefAgencyListableBeanFactory() {
        return chiefAgencyListableBeanFactory;
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setChiefAgencyListableBeanFactory(DefaultListableBeanFactory chiefAgencyListableBeanFactory) {
        ChiefSpringHodler.chiefAgencyListableBeanFactory = chiefAgencyListableBeanFactory;
    }

    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        ChiefSpringHodler.applicationContext = applicationContext;
    }
}
