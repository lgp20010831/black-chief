package com.black.core.asyn;

public class AsynConfigurationManager {

    private static AsynConfiguration configuration;

    public static AsynConfiguration getConfiguration() {
        if (configuration == null){
            configuration = new AsynConfiguration();
        }
        return configuration;
    }

    public static void replaceConfiruration(AsynConfiguration configuration){
        AsynConfigurationManager.configuration = configuration;
    }
}
