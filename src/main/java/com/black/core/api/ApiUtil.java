package com.black.core.api;


import com.black.core.builder.Col;

import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.lang.reflect.Method;
import java.util.*;

public class ApiUtil {

    public static List<String> getMethodHttpMethod(Class<?> controllerClass, Method method) {
        List<String> httpMethods = new ArrayList<>();
        RequestMapping requestMapping;
        if (AnnotationUtils.findAnnotation(method, GetMapping.class) != null){
            httpMethods.add("GET");
        }else if (AnnotationUtils.findAnnotation(method, PostMapping.class) != null){
            httpMethods.add("POST");
        }else if (AnnotationUtils.findAnnotation(method, PutMapping.class) != null){
            httpMethods.add("PUT");
        }else if (AnnotationUtils.findAnnotation(method, DeleteMapping.class) != null){
            httpMethods.add("DELETE");
        }else if ((requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class)) != null){
            RequestMethod[] requestMethods = requestMapping.method();
            Set<String> ms = new HashSet<>();
            for (RequestMethod requestMethod : requestMethods) {
                ms.add(requestMethod.name());
            }
            httpMethods.addAll(ms.isEmpty() ? Col.as("GET", "POST", "PUT", "DELETE") :  ms);
        }
        return httpMethods;
    }


    public static List<String> getMethodUrls(Method method, Class<?> targetClazz){

        String[] path = null;
        RequestMapping resultMapping = AnnotationUtils.findAnnotation(targetClazz, RequestMapping.class);
        if (resultMapping != null) {
            path = resultMapping.value();
        }
        PostMapping postMapping;
        DeleteMapping deleteMapping;
        PutMapping putMapping;
        GetMapping getMapping;
        String[] methodPath;
        if ((postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class)) != null) {
            methodPath = postMapping.value();
        } else if ((deleteMapping = AnnotationUtils.findAnnotation(method, DeleteMapping.class)) != null) {
            methodPath = deleteMapping.value();
        } else if ((putMapping = AnnotationUtils.findAnnotation(method, PutMapping.class)) != null) {
            methodPath = putMapping.value();
        } else if ((getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class)) != null) {
            methodPath = getMapping.value();
        } else if ((resultMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class)) != null) {
            methodPath = resultMapping.value();
        } else {
            throw new RuntimeException("http method url can not parse: " + method);
        }
        ArrayList<String> splicingPath = new ArrayList<>();
        for (String s : methodPath) {
            String mp = s.startsWith("/") ? s : ("/".concat(s));
            if (path != null && path.length != 0) {
                for (String p : path) {
                    splicingPath.add(StringUtils.linkStr(p.startsWith("/") ? p : ("/".concat(p)), mp));
                }
            } else {
                splicingPath.add(mp);
            }
        }
        return splicingPath;
    }

    public static Context createContext(Map<String, Object> source){
        return buildContext(source);
    }

    static ChainHashMap<String,Object> buildMap(){
        return new ChainHashMap<>();
    }

    static Context buildContext(){
        return buildContext(null);
    }

    static Context buildContext(final Map<String, Object> variables){
        Context context = new Context(Locale.CANADA);

        if (variables != null)
            context.setVariables(variables);

        return context;
    }


    public static class ChainHashMap<K,V> extends HashMap<K,V> {

        public ChainHashMap<K, V> chainPut(K key, V value){
            put(key, value);
            return this;
        }

        public ChainHashMap<K, V> chainPutAll(Map<K, V> m){
            putAll(m);
            return this;
        }
    }

}
