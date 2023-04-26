package com.black.core.aop.servlet;

import com.alibaba.fastjson.JSON;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.JsonUtils;
import com.black.core.json.Trust;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class MethodParser {


    public Object[] parse(String jsonName, Method method, Object[] args){
        Map<String, Object> source = null;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (jsonName != null){
                if (parameter.getName().equals(jsonName)){
                    source = convertSource(args[i]);
                    break;
                }
            }else {
                RequestBody requestBody = AnnotationUtils.getAnnotation(parameter, RequestBody.class);
                if (requestBody != null){
                    source = convertSource(args[i]);
                    break;
                }
            }
        }
        if (source == null){
            source = new HashMap<>();
        }

        return doParse(source, method, args);
    }

    protected Object[] doParse(Map<String, Object> source, Method method, Object[] args){
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            RequiredVal requiredBody = AnnotationUtils.getAnnotation(parameter, RequiredVal.class);
            if (requiredBody != null){
                String value = requiredBody.value();
                if (!StringUtils.hasText(value)){
                    value = parameter.getName();
                }
                Object obj;
                if (!source.containsKey(value) && requiredBody.required()){
                    throw new RuntimeException("缺少必需参数: " + value);
                }else {
                    obj = source.get(value);
                }

                if (obj == null){
                    if (requiredBody.required() && !requiredBody.allowNull()){
                        throw new RuntimeException("参数: " + value + " 不能为空");
                    }
                }else {
                    args[i] =  checkValue(parameter, obj);
                }
            }
        }
        return args;
    }

    protected Object checkValue(Parameter parameter, Object val){
        Class<?> type = parameter.getType();
        Class<?> valClass = val.getClass();
        if (!type.isAssignableFrom(valClass)){
            TypeHandler typeHandler = TypeConvertCache.initAndGet();
            if (typeHandler != null){
                val = typeHandler.convert(type, val);
            }
        }
        return val;
    }

    protected Map<String, Object> convertSource(Object arg){
        if (arg == null){
            return new HashMap<>();
        }
        if (arg instanceof Map){
            return (Map<String, Object>) arg;
        }else if (arg instanceof String){
            return JSON.parseObject(arg.toString());
        }else {
            Trust trust = AnnotationUtils.getAnnotation(arg.getClass(), Trust.class);
            if (trust != null){
                return JsonUtils.toJson(arg);
            }
        }
        return new HashMap<>();
    }

}
