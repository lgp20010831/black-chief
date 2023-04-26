package com.black.core.api.tacitly;

import com.black.core.json.Alias;
import com.black.utils.NameUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;

public final class ApiAliasManger {

    private final Map<String, Class<?>> controllerAliasMap = new HashMap<>();

    private final Map<String, Class<?>> pojoALiasMap = new HashMap<>();

    public Class<?> queryController(String alias){
        return controllerAliasMap.get(alias);
    }

    public Class<?> queryPojo(String alias){
        return pojoALiasMap.get(alias);
    }

    public String queryPojoAlias(Class<?> clazz){
        for (String alias : pojoALiasMap.keySet()) {
            Class<?> aClass = pojoALiasMap.get(alias);
            if (aClass.equals(clazz)){
                return alias;
            }
        }
        return null;
    }

    public void registerController(Class<?> controllerClass){
        Alias alias = AnnotationUtils.getAnnotation(controllerClass, Alias.class);
        String name;
        if (alias != null){
            name = alias.value();
        }else {
            name = NameUtil.getName(controllerClass);
        }

        if (controllerAliasMap.containsKey(name)){
            throw new RuntimeException("当前控制器已经存在相同别名， 请用 Alias 注解更换新的别名: " + controllerClass);
        }
        controllerAliasMap.put(name, controllerClass);
    }

    public void registerPojo(Class<?> pojoClass){
        Alias alias = AnnotationUtils.getAnnotation(pojoClass, Alias.class);
        String name;
        if (alias != null){
            name = alias.value();
        }else {
            name = NameUtil.getName(pojoClass);
        }

        if (pojoALiasMap.containsKey(name)){
            throw new RuntimeException("当前实体类已经存在相同别名， 请用 Alias 注解更换新的别名: " + pojoClass);
        }
        pojoALiasMap.put(name, pojoClass);
    }

    public Map<String, Class<?>> getControllerAliasMap() {
        return controllerAliasMap;
    }

    public Map<String, Class<?>> getPojoALiasMap() {
        return pojoALiasMap;
    }
}
