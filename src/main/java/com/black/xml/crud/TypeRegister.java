package com.black.xml.crud;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.json.Trust;
import com.black.core.util.Assert;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.swagger.v2.V2Swagger;
import lombok.Data;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-06-05 13:27
 */
@SuppressWarnings("all") @Data
public class TypeRegister {

    private static TypeRegister register;

    public synchronized static TypeRegister getInstance() {
        if (register == null){
            register = new TypeRegister();
        }
        return register;
    }

    private TypeRegister(){
        registerType(String.class);
        registerType(Integer.class);
        registerType(Double.class);
        registerType(Float.class);
        registerType(Short.class);
        registerType(Long.class);
        registerType(Byte.class);
        registerType(Boolean.class);
        registerType(Character.class);
        registerType(JSONObject.class);
        registerType(Map.class);
        registerType(List.class);
        registerType(Collection.class);
        registerType(JSONArray.class);
        registerType(int.class);
        registerType(char.class);
        registerAliasType("json", JSONObject.class);
        registerAliasType("array", JSONArray.class);

        registerAnnotationType(V2Swagger.class);
        registerAnnotationType(NonNull.class);
        registerAnnotationType(NotNull.class);
        registerAnnotationType(Trust.class);
    }

    private boolean uniqueness = true;

    public final Map<String, Class<?>> typeMappingMap = new ConcurrentHashMap<>();

    public final Map<String, Class<? extends Annotation>> annotationInfoMap = new ConcurrentHashMap<>();

    public void registerAliasType(String alias, Class<?> type){
        typeMappingMap.put(alias, type);
    }

    public void scanAndRegister(String... packageNames){
        ChiefScanner scanner = ScannerManager.getScanner();
        for (String packageName : packageNames) {
            Set<Class<?>> classSet = scanner.load(packageName);
            for (Class<?> type : classSet) {
                if (type.isAnnotation()){
                    registerAnnotationType((Class<? extends Annotation>) type);
                }else {
                    registerType(type);
                }
            }
        }
    }

    public void registerType(Class<?> type){
        String classKey = type.getSimpleName().toLowerCase();
        if (typeMappingMap.containsKey(classKey)){
            if (isUniqueness()){
                typeMappingMap.put(classKey, type);
            }else {
                Class<?> before = typeMappingMap.remove(classKey);
                String beforeKey = before.getName().toLowerCase();
                typeMappingMap.put(beforeKey, before);
                String typeFullKey = type.getName().toLowerCase();
                typeMappingMap.put(typeFullKey, type);
            }
        }else {
            typeMappingMap.put(classKey, type);
        }
    }

    public void registerAnnotationType(Class<? extends Annotation> type){
        String classKey = type.getSimpleName().toLowerCase();
        if (annotationInfoMap.containsKey(classKey)){
            if (isUniqueness()){
                throw new IllegalStateException("Type already exists: " + classKey);
            }else {
                Class<? extends Annotation> before = annotationInfoMap.remove(classKey);
                String beforeKey = before.getName().toLowerCase();
                annotationInfoMap.put(beforeKey, before);
                String typeFullKey = type.getName().toLowerCase();
                annotationInfoMap.put(typeFullKey, type);
            }
        }else {
            annotationInfoMap.put(classKey, type);
        }
    }

    public Class<?> getType(String name){
        Class<?> type = null;
        try {
            type = Class.forName(name);
        } catch (ClassNotFoundException e) {

        }
        if (type != null){
            return type;
        }
        name = name.toLowerCase();
        Map<String, Class<?>> typeMappingMap = getTypeMappingMap();
        type = typeMappingMap.get(name);
        Assert.notNull(type, "can not find type: " + name);
        return type;
    }

    public Class<? extends Annotation> getAnnotationType(String name){
        Class<? extends Annotation> annotationType = null;
        try {
            Class<?> clazz = Class.forName(name);
            if (!clazz.isAnnotation()){
                throw new IllegalStateException("The specified type is not an annotation type: " + name);
            }
            annotationType = (Class<? extends Annotation>) clazz;
        } catch (ClassNotFoundException e) {

        }
        if (annotationType != null){
            return annotationType;
        }
        name = name.toLowerCase();
        Map<String, Class<? extends Annotation>> annotationInfoMap = getAnnotationInfoMap();
        annotationType = annotationInfoMap.get(name);
        Assert.notNull(annotationType, "can not load annotation type: " + name);
        return annotationType;
    }

    public void clear(){
        typeMappingMap.clear();
        annotationInfoMap.clear();
    }
}
