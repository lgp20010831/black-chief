package com.black.premission;

public class GlobalRUPConfigurationHolder {

    private static GlobalRUPConfiguration configuration;

    public static void setConfiguration(GlobalRUPConfiguration configuration) {
        GlobalRUPConfigurationHolder.configuration = configuration;
    }

    public static GlobalRUPConfiguration getConfiguration() {
        if (configuration == null){
            configuration = new GlobalRUPConfiguration();
        }
        return configuration;
    }

}
