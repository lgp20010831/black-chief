package com.black.core.config;

import com.black.utils.ServiceUtils;

import java.io.InputStream;
import java.util.Map;

public interface ApplicationConfigurationReader {

    String getApplicationName();

    String getActiveProfile();

    String getApplicationActiveName();

    default InputStream getApplicationInputStream(){
        String applicationName = getApplicationName();
        if (applicationName != null){
            return ServiceUtils.getResource(applicationName);
        }
        return null;
    }

    default InputStream getApplicationActiveInputStream(){
        String applicationActiveName = getApplicationActiveName();
        if (applicationActiveName != null){
            return ServiceUtils.getResource(applicationActiveName);
        }
        return null;
    }

    Map<String, String> getMasterAndSubApplicationConfigSource();

    Map<String, String> getMasterApplicationConfigSource();

    Map<String, String> getSubApplicationConfigSource();

    String selectAttribute(String name);

    <T> T fullConfig(T config);

    <T> T fullConfig(T config, boolean force);

    default Map<String, String> groupQueryForSub(String command){
        return groupQueryForSub(command, true);
    }

    default Map<String, String> groupQueryForSub(String command, boolean removePrefix){
        return groupQueryForMap(getSubApplicationConfigSource(), command, removePrefix);
    }

    default Map<String, String> groupQueryForMaster(String command){
        return groupQueryForMaster(command, true);
    }

    default Map<String, String> groupQueryForMaster(String command, boolean removePrefix){
        return groupQueryForMap(getMasterApplicationConfigSource(), command, removePrefix);
    }

    default Map<String, String> groupQueryForGlobal(String command){
        return groupQueryForMaster(command, true);
    }

    default Map<String, String> groupQueryForGlobal(String command, boolean removePrefix){
        return groupQueryForMap(getMasterAndSubApplicationConfigSource(), command, removePrefix);
    }

    Map<String, String> groupQueryForMap(Map<String, String> source, String command, boolean removePrefix);
}
