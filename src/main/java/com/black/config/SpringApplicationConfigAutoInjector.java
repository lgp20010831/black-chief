package com.black.config;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;

import java.util.Map;

public class SpringApplicationConfigAutoInjector extends CentralizedProcessorConfiguringAttributeInjector{

    public SpringApplicationConfigAutoInjector(){
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        Map<String, String> source = reader.getMasterAndSubApplicationConfigSource();
        setDataSource(source);
    }

}
