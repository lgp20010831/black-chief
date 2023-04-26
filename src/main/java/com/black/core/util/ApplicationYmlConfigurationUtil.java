package com.black.core.util;

import com.black.core.spring.ApplicationHolder;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public final class ApplicationYmlConfigurationUtil {

    public static String ACTIVE_PROFILE;
    public static boolean yaml = false;
    public static final String APPLICATION_ALIAS = "application.yml";
    public static final String APPLICATION_YAML_ALIAS = "application.yaml";
    public static final String APPLICATION_PREFIX = "application";
    public static final String APPLICATION_CONNECTOR = "-";
    public static final String APPLICATION_PROFILES_ACTIVE = "spring.profiles.active";
    public static final String APPLICATION_ENDING = ".yml";
    public static final String APPLICATION_YAML_ENDING = ".yaml";
    private final static Map<String, Map<String, String>> applicationYmlCache = new HashMap<>();

    public static String getProfilesActive(){
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
            Map<String, String> source = obtainYmlSource(yaml ? APPLICATION_YAML_ALIAS : APPLICATION_ALIAS);
            ACTIVE_PROFILE = source.get(APPLICATION_PROFILES_ACTIVE);
        }
        return ACTIVE_PROFILE;
    }

    public static Map<String, String> obtainYmlSource(){
        return obtainYmlSource(null);
    }

    public static Map<String, String> obtainYmlSource(String name){
        String applicationName = getApplicationName(name);
        return applicationYmlCache.computeIfAbsent(applicationName, YmlUtil::getYmlByFileName);
    }

    public static Map<String, String> loadYmlSource(String path){
        return YmlUtil.getYmlByFileName(path);

    }

    public static String getApplicationName(String name){
        String applicationName;
        if (name == null){
            return yaml ? APPLICATION_YAML_ALIAS : APPLICATION_ALIAS;
        }else if (!name.startsWith(APPLICATION_PREFIX)){
            if (name.startsWith(APPLICATION_CONNECTOR)){
                applicationName = APPLICATION_PREFIX + name;
            }else {
                applicationName = APPLICATION_PREFIX + APPLICATION_CONNECTOR + name;
            }
        }else {
            applicationName = name;
        }
        if (!applicationName.endsWith(yaml ? APPLICATION_YAML_ENDING : APPLICATION_ENDING)){
            applicationName = applicationName + (yaml ? APPLICATION_YAML_ALIAS : APPLICATION_ENDING);
        }
        return applicationName;
    }
}
