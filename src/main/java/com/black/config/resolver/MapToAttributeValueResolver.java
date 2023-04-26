package com.black.config.resolver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.FormatParser;
import com.black.config.AttributeValue;
import com.black.config.AttributeValueWrapper;
import com.black.core.chain.GroupKeys;
import com.black.core.json.UCJsonParser;
import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter @Getter
public class MapToAttributeValueResolver {

    private String elementSeparator = ".";

    private final FormatParser formatParser;

    public MapToAttributeValueResolver() {
        formatParser = new FormatParser();
        UCJsonParser parser = formatParser.getParser();
        parser.setMatching('=');
    }

    protected String getTopName(String text){
        int index = text.indexOf(elementSeparator);
        String str = index == -1 ? text : text.substring(0, index);
        str = StringUtils.ruacnl(str, '-');
        return str;
    }

    protected String getChildNames(String text){
        int index = text.indexOf(elementSeparator);
        return index == -1 ? "" : StringUtils.removeIfStartWith(text.substring(index), elementSeparator);
    }

    public AttributeValueWrapper parse(Map<String, String> datasource){
        Map<String, AttributeValue> result = new LinkedHashMap<>();
        Map<GroupKeys, AttributeValue> globalCache = new LinkedHashMap<>();
        int level = 1;
        for (String name : datasource.keySet()) {
            String topName = getTopName(name);
            String childNames = getChildNames(name);
            String valueString = datasource.get(name);
            AttributeValue attributeValue = globalCache.computeIfAbsent(new GroupKeys(topName, level), g -> {
                return new AttributeValue(topName);
            });
            attributeValue.setLevel(level);
            attributeValue.setElementSeparator(elementSeparator);
            if (hasChild(childNames)){
                handlerChild(attributeValue, childNames, globalCache, valueString, level + 1);
            }else {
                handleAttributeValue(attributeValue, valueString);
            }
            result.put(topName, attributeValue);
        }
        AttributeValueWrapper wrapper = new AttributeValueWrapper();
        wrapper.setAttributeValueMap(result);
        wrapper.setGlobalCache(globalCache);
        return wrapper;
    }

    protected void handleAttributeValue(AttributeValue attributeValue, String text){
        Object parsedObject = formatParser.parseObject(text);
        if (parsedObject instanceof JSONObject){
            attributeValue.setValueMap(true);
            attributeValue.setJsonObject((JSONObject) parsedObject);
        }

        if (parsedObject instanceof JSONArray){
            attributeValue.setValueList(true);
            attributeValue.setJsonArray((JSONArray) parsedObject);
        }

        attributeValue.setValue(text);
    }

    protected void handlerChild(AttributeValue attributeValue, String text,
                                Map<GroupKeys, AttributeValue> globalCache, String value, int level){
        String name = getTopName(text);
        String valueString = getChildNames(text);
        AttributeValue child = globalCache.computeIfAbsent(new GroupKeys(name, level), g -> {
            return new AttributeValue(name);
        });
        child.setLevel(level);
        child.setElementSeparator(elementSeparator);
        attributeValue.putChild(child);
        child.setParent(attributeValue);
        if (hasChild(valueString)){
            handlerChild(child, valueString, globalCache, value, level + 1);
        }else {
            handleAttributeValue(child, value);
        }
    }

    protected boolean hasChild(String value){
        return value.contains(elementSeparator) || StringUtils.hasText(value);
    }

}
