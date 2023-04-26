package com.black.core.spring;

import com.black.holder.SpringHodler;
import com.black.core.spring.pureness.CrutchSpringPettyApplication;

public class ChiefApplicationBuilder {


    public static ChiefExpansivelyApplication createApplication(Object configuration){
        ChiefExpansivelyApplication application;
        try {
            Class.forName("org.springframework.cglib.proxy.MethodInterceptor");
            application = new CrutchSpringPettyApplication(configuration);
            CrutchSpringPettyApplication crutchSpringPettyApplication = (CrutchSpringPettyApplication) application;
            crutchSpringPettyApplication.setApplicationContext(SpringHodler.getApplicationContext());
            crutchSpringPettyApplication.setBeanFactory(SpringHodler.getListableBeanFactory());
        } catch (ClassNotFoundException e) {
            application = new PettySpringApplication(configuration);
        }
        return application;
    }

}
