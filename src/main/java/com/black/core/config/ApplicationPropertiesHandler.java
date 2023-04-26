package com.black.core.config;

import com.black.core.spring.ApplicationHolder;
import com.black.core.util.*;
import com.black.core.yml.YmlConfigurationProperties;
import com.black.utils.ReflexHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.black.core.util.ApplicationYmlConfigurationUtil.*;

public class ApplicationPropertiesHandler implements ApplicationConfigurationReader{

    private Map<String, String> masterApplicationConfigSource;

    private Map<String, String> subApplicationConfigSource;

    private Map<String, String> masterAndSubApplicationConfigSource;

    private String activeName;

    private String masterApplicationFileName;

    private String subApplicationFileName;

    public ApplicationPropertiesHandler(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties pro = getPro(loader.getResourceAsStream("application.properties"));
        String ap = getAp(pro);
        setActiveName(activeName);
        if (StringUtils.hasText(ap)){
            String proNae = getApplicationName(ap);
            setMasterApplicationFileName(null);
            setSubApplicationFileName(getApplicationName(ap));
            InputStream in = loader.getResourceAsStream(proNae);
            if (in == null){
                in = loader.getResourceAsStream(ApplicationYmlConfigurationUtil.getApplicationName(ap));
            }
            if (in == null){
                masterApplicationConfigSource = new HashMap<>();
            }else {
                masterApplicationConfigSource = Utils.castMap(getPro(in));
            }

            subApplicationConfigSource = Utils.castMap(pro);
        }else {
            masterApplicationConfigSource = Utils.castMap(pro);
        }
    }

    public static Properties getPro(InputStream in){
        Properties pro = new Properties();
        try {
            pro.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pro;
    }


    String getAp(Properties pro){
        if (ACTIVE_PROFILE != null){
            return ACTIVE_PROFILE;
        }
        ApplicationContext context = ApplicationHolder.getApplicationContext();
        if(context != null){
            String[] activeProfiles = context.getEnvironment().getActiveProfiles();
            if(activeProfiles != null && activeProfiles.length != 0){
                ACTIVE_PROFILE = activeProfiles[0];
            }
        }else {
            ACTIVE_PROFILE = (String) pro.get(APPLICATION_PROFILES_ACTIVE);
        }
        return ACTIVE_PROFILE;
    }

    public static String getApplicationName(String name){
        String applicationName;
        if (name == null){
            return "application.properties";
        }else if (!name.startsWith(APPLICATION_PREFIX)){
            if (name.startsWith(APPLICATION_CONNECTOR)){
                applicationName = APPLICATION_PREFIX + name;
            }else {
                applicationName = APPLICATION_PREFIX + APPLICATION_CONNECTOR + name;
            }
        }else {
            applicationName = name;
        }
        if (!applicationName.endsWith(".properties")){
            applicationName = applicationName + ".properties";
        }
        return applicationName;
    }


    @Override
    public String getApplicationName() {
        return getApplicationActiveName();
    }

    @Override
    public String getActiveProfile() {
        return getActiveName();
    }

    @Override
    public String getApplicationActiveName() {
        return getSubApplicationFileName();
    }

    @Override
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

    @Override
    public Map<String, String> getMasterApplicationConfigSource() {
        return masterApplicationConfigSource;
    }

    @Override
    public Map<String, String> getSubApplicationConfigSource() {
        return subApplicationConfigSource;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public void setSubApplicationFileName(String subApplicationFileName) {
        this.subApplicationFileName = subApplicationFileName;
    }

    public void setMasterApplicationFileName(String masterApplicationFileName) {
        this.masterApplicationFileName = masterApplicationFileName;
    }

    public String getActiveName() {
        return activeName;
    }

    public String getMasterApplicationFileName() {
        return masterApplicationFileName;
    }

    public String getSubApplicationFileName() {
        return subApplicationFileName;
    }

    @Override
    public String selectAttribute(String name) {
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


    @Override
    public <T> T fullConfig(T config) {
        return fullConfig(config, false);
    }

    @Override
    public <T> T fullConfig(T config, boolean force) {
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
