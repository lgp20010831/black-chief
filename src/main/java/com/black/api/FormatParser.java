package com.black.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.json.UCJsonParser;
import com.black.core.query.ClassWrapper;
import com.black.core.util.StringUtils;

import java.util.*;

public class FormatParser {

    private final UCJsonParser parser;
    private final Configuration configuration;
    private final Map<String, ClassWrapper<?>> dependencyMap;


    public FormatParser(){
        this(null);
    }

    public FormatParser(Configuration configuration) {
        this.configuration = configuration;
        this.dependencyMap = configuration == null ? new HashMap<>() : configuration.getDependencyMap();
        parser = new UCJsonParser();

    }


    public JSON parseJSON(String format){
        format = StringUtils.removeFrontSpace(format);
        if (format.startsWith("{")) {
            return parser.parseJson(format);
        }else if (format.startsWith("[")){
            return parser.parseArray(format);
        }else {
            throw new IllegalStateException("格式应该为json或者array: " + format);
        }
    }

    public Object parseObject(String txt){
        String format = txt;
        format = StringUtils.removeFrontSpace(format);
        if (format.startsWith("{")) {
            return parseJsonObject(format);
        }else if (format.startsWith("[")){
            return parseJsonArray(format);
        }else {
            return txt;
        }
    }

    public String parse(String format){
        format = StringUtils.removeFrontSpace(format);
        if (format.startsWith("{")) {
            return parseJson(format);
        }else if (format.startsWith("[")){
            return parseArray(format);
        }else {
            throw new IllegalStateException("格式应该为json或者array: " + format);
        }
    }

    public UCJsonParser getParser() {
        return parser;
    }


    public JSONObject parseJsonObject(String format){
        return parser.parseJson(format);
    }

    public JSONArray parseJsonArray(String format){
        return parser.parseArray(format);
    }

    public String parseJson(String format){
        JSONObject json = parser.parseJson(format);

        return JSONTool.formatJson(handlerDependencyJson(json).toString());
    }

    public String parseArray(String format){
        JSONArray array = parser.parseArray(format);
        return JSONTool.formatJson(handlerDependencyArray(array).toString());
    }

    public Collection<Object> handlerDependencyArray(Collection<Object> collection){
        Collection<Object> newArray = new JSONArray();
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof Collection){
                newArray.add(handlerDependencyArray((Collection<Object>) element));
            }else if (element instanceof Map){
                newArray.add(handlerDependencyJson((Map<String, Object>) element));
            }else {
                String eleStr = element.toString();
                if (dependencyMap.containsKey(eleStr)){
                    JSONObject json = new JSONObject();
                    ClassWrapper<?> wrapper = dependencyMap.get(eleStr);
                    iterator.remove();
                    handler0(json, wrapper);
                    newArray.add(json);
                }else {
                    newArray.add(element);
                }
            }
        }
        return newArray;
    }

    public Map<String, Object> handlerDependencyJson(Map<String, Object> map){
        Map<String, Object> newMap = new JSONObject();
        for (String key : map.keySet()) {
            Object val = map.get(key);
            if (dependencyMap.containsKey(key)){
                ClassWrapper<?> wrapper = dependencyMap.get(key);
                handler0(newMap, wrapper);
            }else

            if (val instanceof Collection){
                Collection<Object> collection = handlerDependencyArray((Collection<Object>) val);
                newMap.put(key, collection);
            }else

            if (val instanceof Map){
                Map<String, Object> json = handlerDependencyJson((Map<String, Object>) val);
                newMap.put(key, json);
            }
            else
                newMap.put(key, val);
        }
        return newMap;
    }

    public void handler0(Map<String, Object> map, ClassWrapper<?> dependencyClass){
        for (String fieldName : dependencyClass.getFieldNames()) {
            if (Number.class.isAssignableFrom(dependencyClass.getFieldType(fieldName))) {
                map.put(fieldName, 1);
            }else {
                map.put(fieldName, "xxxxxx");
            }
        }
    }
}
