package com.black.core.factory.beans.config_collect520;

import com.black.config.CentralizedProcessorConfiguringAttributeInjector;
import com.black.config.SpringApplicationConfigAutoInjector;
import com.black.core.factory.beans.vsf.VfsBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Map;

@SuppressWarnings("all")
public class AttributeInjectionEnhancementBeanFactory extends VfsBeanFactory {

    private CentralizedProcessorConfiguringAttributeInjector configuringAttributeInjector;

    public AttributeInjectionEnhancementBeanFactory(){
        this(null);
    }

    public AttributeInjectionEnhancementBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
        registerBeanFactoryProcessor(new NestPropertyFieldHandler());
        registerBeanFactoryProcessor(new NestPropertyParamHandler());
    }

    public CentralizedProcessorConfiguringAttributeInjector getConfiguringAttributeInjector() {
        if (configuringAttributeInjector == null){
            configuringAttributeInjector = new CentralizedProcessorConfiguringAttributeInjector();
        }
        return configuringAttributeInjector;
    }

    public void setConfiguringAttributeInjector(CentralizedProcessorConfiguringAttributeInjector configuringAttributeInjector) {
        this.configuringAttributeInjector = configuringAttributeInjector;
    }

    public void reloadSource(Map<String, String> source){
        getConfiguringAttributeInjector().setDataSource(source);
    }
    public void pourinto(Object target){
        getConfiguringAttributeInjector().pourintoBean(target);
    }
}
