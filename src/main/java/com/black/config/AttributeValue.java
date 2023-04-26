package com.black.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
public class AttributeValue {

    private AttributeValue parent;

    private final String name;

    private boolean map = false;

    private boolean valueMap = false;

    private boolean valueList = false;

    private JSONObject jsonObject;

    private JSONArray jsonArray;

    private String value;

    private int level;

    private String elementSeparator = ".";

    private Map<String, AttributeValue> mapChilds = new LinkedHashMap<>();

    public AttributeValue(String name) {
        this.name = name;
    }

    public void setMapChilds(Map<String, AttributeValue> mapChilds) {
        this.mapChilds = mapChilds;
        if (mapChilds != null){
            map = true;
        }
    }

    public void putChild(AttributeValue value){
        mapChilds.put(value.getName(), value);
        map = true;
    }

    public String getPath(){
        return parent == null ? name : parent.getPath() + elementSeparator + name;
    }

    public Map<String, String> toSource(){
        return toSource(true);
    }

    public Map<String, String> toSource(boolean prefix){
        if (isMap()){
            Map<String, String> result = new LinkedHashMap<>();
            for (String cn : mapChilds.keySet()) {
                AttributeValue attributeValue = mapChilds.get(cn);
                Map<String, String> source = attributeValue.toSource();
                source.forEach((k, v) -> {
                    result.put(prefix ? name + elementSeparator + k : k, v);
                });
            }
            return result;
        }else {
            String val = value;
            if (isValueMap()){
                val = jsonObject.toString();
            }else if (isValueList()){
                val = jsonArray.toString();
            }
            return ServiceUtils.ofMap(name, val);
        }
    }
}
