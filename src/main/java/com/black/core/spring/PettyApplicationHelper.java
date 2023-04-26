package com.black.core.spring;

import com.black.core.config.AbstractConfiguration;
import com.black.core.spring.pureness.CrutchSpringPettyApplication;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

public class PettyApplicationHelper {


    ApplicationContext applicationContext;

    BeanFactory beanFactory;

    public PettyApplicationHelper(ApplicationContext applicationContext, BeanFactory beanFactory) {
        this.applicationContext = applicationContext;
        this.beanFactory = beanFactory;
        ApplicationHolder.applicationContext = applicationContext;
        ApplicationHolder.beanFactory = beanFactory;
    }

    public ChiefExpansivelyApplication createApplication(AbstractConfiguration configuration){
        ChiefExpansivelyApplication chiefExpansivelyApplication;
        try {
            Class.forName("org.springframework.beans.factory.BeanFactory");
            chiefExpansivelyApplication = new CrutchSpringPettyApplication(configuration);
        } catch (ClassNotFoundException e) {
            chiefExpansivelyApplication = new PettySpringApplication(configuration);
        }
        chiefExpansivelyApplication.pattern();
        return chiefExpansivelyApplication;
    }
}
