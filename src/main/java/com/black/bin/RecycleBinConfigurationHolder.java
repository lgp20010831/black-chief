package com.black.bin;

public class RecycleBinConfigurationHolder {

    private static Configuration configuration;


    public static Configuration getConfiguration() {
        if(configuration == null){
            configuration = new Configuration();
        }
        return configuration;
    }

    public static void setConfiguration(Configuration configuration) {
        RecycleBinConfigurationHolder.configuration = configuration;
    }
}
