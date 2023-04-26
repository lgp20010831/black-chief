package com.black.core.yml;

import com.black.core.util.ApplicationYmlConfigurationUtil;

import java.util.Map;

public class ChiefConfigHideYmlHandler extends ApplicationYmlConfigurationHandler{


    private static ChiefConfigHideYmlHandler configHideYmlHandler;


    public synchronized static ChiefConfigHideYmlHandler getInstance() {
        if (configHideYmlHandler == null){
            configHideYmlHandler = new ChiefConfigHideYmlHandler();
        }
        return configHideYmlHandler;
    }

    public ChiefConfigHideYmlHandler(){
        Map<String, String> source = ApplicationYmlConfigurationUtil.loadYmlSource("chief_config_hide.yml");
        setMasterApplicationConfigSource(source);
    }
}
