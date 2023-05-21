package com.black.core.factory.beans.config_collect520;

import com.black.config.CentralizedProcessorConfiguringAttributeInjector;
import com.black.config.SpringApplicationConfigAutoInjector;
import com.black.core.factory.beans.vsf.VfsBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Map;

@SuppressWarnings("all")
public class AttributeInjectionEnhancementBeanFactory extends VfsBeanFactory {

    private final CentralizedProcessorConfiguringAttributeInjector configuringAttributeInjector;

    public AttributeInjectionEnhancementBeanFactory(){
        this(null);
    }

    public AttributeInjectionEnhancementBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
        configuringAttributeInjector = new SpringApplicationConfigAutoInjector();
        registerBeanFactoryProcessor(new NestPropertyFieldHandler());
        registerBeanFactoryProcessor(new NestPropertyParamHandler());
    }

    public CentralizedProcessorConfiguringAttributeInjector getConfiguringAttributeInjector() {
        return configuringAttributeInjector;
    }

    public void reloadSource(Map<String, String> source){
        configuringAttributeInjector.setDataSource(source);
    }

    public void pourinto(Object target){
        getConfiguringAttributeInjector().pourintoBean(target);
    }
}
