package com.black.callback;

import com.black.core.ibatis.IbatisBeanEnhancedComponents;
import com.black.spring.mapping.UrlMappingHandler;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

public class ServletMappingHandlerManager implements SpringApplicationRunListener {


    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        ApplicationStartingTaskManager.addTask(UrlMappingHandler::deposit);
        //ApplicationStartingTaskManager.addTask(IbatisBeanEnhancedComponents::handleIbatisBean);
    }
}
