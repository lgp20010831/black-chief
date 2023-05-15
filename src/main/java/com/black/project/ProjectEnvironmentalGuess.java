package com.black.project;

import com.black.spring.ChiefSpringHodler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import springfox.documentation.spring.web.plugins.Docket;

@SuppressWarnings("all") @Data @Builder @AllArgsConstructor
public class ProjectEnvironmentalGuess {


    private boolean inSwagger;

    private boolean joinChiefApi;

    private boolean simpleController = true;

    private boolean createImpl = true;

    private boolean createMapper = true;

    private boolean createController = true;

    private boolean createPojo = true;

    public ProjectEnvironmentalGuess(){
        joinChiefApi = false;
        DefaultListableBeanFactory factory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        if (factory != null){
            //判断当前项目是否启用了 swagger
            try {
                Docket docket = factory.getBean(Docket.class);
                inSwagger = docket.isEnabled();
            }catch (Throwable e){
                inSwagger = false;
            }

        }else {
            inSwagger = true;
        }
    }
}
