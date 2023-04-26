package com.black.config;

import java.util.Map;

public class ConfigTool {


    public static ConfiguringAttributeAutoinjector create(Map<String, String> source){
        CentralizedProcessorConfiguringAttributeInjector injector = new CentralizedProcessorConfiguringAttributeInjector();
        injector.setDataSource(source);
        return injector;
    }


}
