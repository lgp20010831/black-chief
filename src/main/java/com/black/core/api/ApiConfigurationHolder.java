package com.black.core.api;

public class ApiConfigurationHolder {

    private static ApiConfiguration apiConfiguration;

    public static ApiConfiguration getConfiguration(){
        if (apiConfiguration == null){
            apiConfiguration = new ApiConfiguration();
        }
        return apiConfiguration;
    }

}
