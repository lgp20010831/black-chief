package com.black.core.yml;

import com.black.core.util.ApplicationYmlConfigurationUtil;
import com.black.utils.ServiceUtils;

public class ApplicationYmlConfigurationHandlerHolder {

    protected static ApplicationYmlConfigurationHandler ymlConfigurationHandler;

    public static ApplicationYmlConfigurationHandler getYmlConfigurationHandler() {
        if (ymlConfigurationHandler == null){
            ymlConfigurationHandler = new ApplicationYmlConfigurationHandler();
            if (ServiceUtils.existResource("application.yaml")){
                ApplicationYmlConfigurationUtil.yaml = true;
            }
            ymlConfigurationHandler.init();
        }
        return ymlConfigurationHandler;
    }
}
