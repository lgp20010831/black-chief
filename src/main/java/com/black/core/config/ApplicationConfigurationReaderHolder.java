package com.black.core.config;

import com.black.core.SpringAutoThymeleafApplication;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.yml.ApplicationYmlConfigurationHandlerHolder;
import com.black.core.yml.ChiefConfigHideYmlHandler;
import com.black.utils.ServiceUtils;


public class ApplicationConfigurationReaderHolder {

    private static ApplicationConfigurationReader reader;


    public static ApplicationConfigurationReader getReader(){
        if (reader == null){
            if (ServiceUtils.getResource("application.yaml") != null ||
                    ServiceUtils.getResource("application.yml") != null) {
                return ApplicationYmlConfigurationHandlerHolder.getYmlConfigurationHandler();
            }else if (Thread.currentThread().getContextClassLoader().getResource("application.properties") != null){
                return ApplicationPropertiesHandlerHolder.getHandler();
            }else {
                Class<?> mainClass = ChiefApplicationRunner.getMainClass();
                if (mainClass != null && !mainClass.equals(SpringAutoThymeleafApplication.class)){
                    throw new IllegalStateException("无法找到 application.yml 或者 application.properties 配置文件");
                }
                return reader = ChiefConfigHideYmlHandler.getInstance();
            }

        }
        return reader;
    }

}
