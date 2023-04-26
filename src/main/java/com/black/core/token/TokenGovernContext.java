package com.black.core.token;

import com.black.role.Configuration;
import com.black.role.ConfigurationBiko;
import com.black.role.ConfigurationHolder;
import com.black.role.TokenPattern;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;

@LoadSort(1450)
@LazyLoading(EnabledTokenGovern.class)
public class TokenGovernContext implements OpenComponent {

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        EnabledTokenGovern annotation = mainClass.getAnnotation(EnabledTokenGovern.class);
        if (annotation != null){
            Configuration configuration = new Configuration();
            TokenPattern pattern = annotation.pattern();
            pattern.getPattern().accept(configuration);
            Class<? extends ConfigurationBiko> biko = annotation.biko();
            if (!biko.equals(ConfigurationBiko.class)){
                FactoryManager.init();
                ConfigurationBiko configurationBiko = FactoryManager.getBeanFactory().getSingleBean(biko);
                configurationBiko.biko(configuration);
            }
            ConfigurationHolder.setConfiguration(configuration);
        }
    }
}
