package com.black.core.yml;


import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationPropertiesHandler;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.util.*;
import com.black.utils.ReflexHandler;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Setter
public class ApplicationYmlConfigurationHandler implements ApplicationDriver, ApplicationConfigurationReader {

    private Map<String, String> masterApplicationConfigSource;

    private Map<String, String> subApplicationConfigSource;

    private Map<String, String> masterAndSubApplicationConfigSource;

    private String activeName;

    private String masterApplicationFileName;

    private String subApplicationFileName;

    public ApplicationYmlConfigurationHandler(){

    }

    public ApplicationYmlConfigurationHandler(Map<String, String> master, Map<String, String> sub){
        masterApplicationConfigSource = master;
        subApplicationConfigSource = sub;
    }

    public void init(){
        //初始化两个资源
        String profilesActive = ApplicationYmlConfigurationUtil.getProfilesActive();
        System.out.println("ApplicationYmlConfigurationHandler cognizance active profile is " + profilesActive);
        setActiveName(profilesActive);
        setMasterApplicationFileName(ApplicationYmlConfigurationUtil.getApplicationName(null));
        setSubApplicationFileName(ApplicationYmlConfigurationUtil.getApplicationName(profilesActive));
        if (!StringUtils.hasText(profilesActive)){
            masterApplicationConfigSource = ApplicationYmlConfigurationUtil.obtainYmlSource();
        }else {
            try {
                masterApplicationConfigSource = ApplicationYmlConfigurationUtil.obtainYmlSource(profilesActive);
            }catch (RuntimeException e){
                String proName = ApplicationPropertiesHandler.getApplicationName(profilesActive);
                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(proName);
                if (in != null){
                    masterApplicationConfigSource = Utils.castMap(ApplicationPropertiesHandler.getPro(in));
                }else {
                    masterApplicationConfigSource = new HashMap<>();
                }
            }
            subApplicationConfigSource = ApplicationYmlConfigurationUtil.obtainYmlSource();
        }
    }


    @Override
    public String getApplicationName() {
        return getMasterApplicationFileName();
    }

    @Override
    public String getActiveProfile() {
        return getActiveName();
    }

    @Override
    public String getApplicationActiveName() {
        return getSubApplicationFileName();
    }

    public Map<String, String> getMasterAndSubApplicationConfigSource() {
        if (masterAndSubApplicationConfigSource == null){
            masterAndSubApplicationConfigSource = new HashMap<>();
            Map<String, String> subSource = getSubApplicationConfigSource();
            if (subSource != null){
                masterAndSubApplicationConfigSource.putAll(subSource);
            }
            Map<String, String> masterSource = getMasterApplicationConfigSource();
            if (masterSource != null){
                masterAndSubApplicationConfigSource.putAll(masterSource);
            }
        }
        return masterAndSubApplicationConfigSource;
    }

    public Map<String, String> getMasterApplicationConfigSource() {
        return masterApplicationConfigSource;
    }

    public Map<String, String> getSubApplicationConfigSource() {
        return subApplicationConfigSource;
    }

    public String getActiveName() {
        return activeName;
    }

    public String getSubApplicationFileName() {
        return subApplicationFileName;
    }

    public String getMasterApplicationFileName() {
        return masterApplicationFileName;
    }

    public void setSubApplicationFileName(String subApplicationFileName) {
        this.subApplicationFileName = subApplicationFileName;
    }

    public void setMasterApplicationFileName(String masterApplicationFileName) {
        this.masterApplicationFileName = masterApplicationFileName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String selectAttribute(String name){
        String result = null;
        if (masterApplicationConfigSource != null){
            result = masterApplicationConfigSource.get(name);
        }
        if (result == null){
            if (subApplicationConfigSource != null){
                result = subApplicationConfigSource.get(name);
            }
        }
        return result;
    }

    public <T> T fullConfig(T config){
        return fullConfig(config, false);
    }

    public <T> T fullConfig(T config, boolean force){
        try {
            Map<String, Field> variableKeys = loadVariableKeys(config);
            String profilesActive = ApplicationYmlConfigurationUtil.getProfilesActive();
            Map<String, String> ymlSource = ApplicationYmlConfigurationUtil.obtainYmlSource(profilesActive);
            asConfig(config, variableKeys, ymlSource, force);
            Map<String, String> applicationSource = ApplicationYmlConfigurationUtil.obtainYmlSource(ApplicationYmlConfigurationUtil.APPLICATION_ALIAS);
            asConfig(config, variableKeys, applicationSource, force);
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
        }
        return config;
    }


    public Map<String, String> groupQueryForMap(Map<String, String> source, String command, boolean removePrefix){
        if (!StringUtils.hasText(command)) {
            return source;
        }

        command = StringUtils.addIfNotEndWith(command, ".");
        Map<String, String> result = new HashMap<>();
        for (String key : source.keySet()) {
             if (key.startsWith(command)){
                 result.put(removePrefix ? StringUtils.removeIfStartWith(key, command) : key, source.get(key));
             }
        }
        return result;
    }

    protected void asConfig(Object config,
                            Map<String, Field> variableKeys,
                            Map<String, String> ymlSource,
                            boolean force) throws IllegalAccessException {
        for (String alias : variableKeys.keySet()) {
            Field f = variableKeys.get(alias);
            if (!Modifier.isFinal(f.getModifiers())){
                String value = ymlSource.get(alias);
                if (value != null){
                    if (force || isNullValue(f, config)){
                        SetGetUtils.invokeSetMethod(f, value, config);
                    }
                }
            }
        }
    }

    private boolean isNullValue(Field f, Object obj) throws IllegalAccessException {
        return f.get(obj) == null;
    }

    private Map<String, Field> loadVariableKeys(Object config){
        final Class<?> clazz = config.getClass();
        List<Field> fields = ReflexHandler.getAccessibleFields(config);
        String prefix = getPrefix(clazz);
        Map<String, Field> result = new HashMap<>();
        for (Field field : fields) {
            String name = field.getName();
            if (prefix != null){
                result.put(StringUtils.linkStr(prefix, ".", name), field);
            }else {
                result.put(name, field);
            }
        }
        return result;
    }

    private String getPrefix(Class<?> clazz){
        String prefix = null;
        ConfigurationProperties properties = AnnotationUtils.getAnnotation(clazz, ConfigurationProperties.class);
        if (properties != null){
            prefix = properties.value();
        }
        YmlConfigurationProperties ymlConfigurationProperties = AnnotationUtils.getAnnotation(clazz, YmlConfigurationProperties.class);
        if (ymlConfigurationProperties != null){
            prefix = ymlConfigurationProperties.value();
        }
        return prefix;
    }

}
