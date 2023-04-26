package com.black.config;

import java.util.Map;

public interface ConfiguringAttributeAutoinjector {

    Map<String, String> getDataSource();

    Map<String, AttributeValue> getPathAttributeValues();

    void setDataSource(Map<String, String> dataSource);

    ConfiguringAttributeAutoinjector copy();

    Environment getEnvironment();

    void setParseMethod(boolean parseMethod);

    boolean isParseMethod();

    void pourintoBean(Object bean);

    void setAttributeSeparator(String separator);

    String selectAttribute(String name);

    AttributeValue selectAttributeValue(String path);

    default Map<String, AttributeValue> selectGroupAttributes(String command){
        return selectGroupAttributes(command, true);
    }

    default Map<String, AttributeValue> selectGroupAttributes(String command, boolean removePrefix){
        return selectGroupAttributes(getDataSource(), command, removePrefix);
    }

    Map<String, AttributeValue> selectGroupAttributes(Map<String, String> source, String command, boolean removePrefix);


    default Map<String, String> selectGroupSources(String command){
        return selectGroupSources(command, true);
    }

    default Map<String, String> selectGroupSources(String command, boolean removePrefix){
        return selectGroupSources(getDataSource(), command, removePrefix);
    }

    Map<String, String> selectGroupSources(Map<String, String> source, String command, boolean removePrefix);


}
