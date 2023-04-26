package com.black.config;

import com.black.config.resolver.MapToAttributeValueResolver;
import com.black.core.log.IoLog;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
public abstract class AbstractConfiguringAttributeAutoinjector implements ConfiguringAttributeAutoinjector{

    protected Map<String, String> dataSource;

    protected boolean parseMethod;

    protected Map<String, AttributeValue> attributeValues = new LinkedHashMap<>();

    protected Map<String, AttributeValue> pathAttributeValues = new LinkedHashMap<>();

    protected String attributeSeparator = ".";

    protected final Environment environment;

    protected final MapToAttributeValueResolver toAttributeValueResolver;

    public AbstractConfiguringAttributeAutoinjector() {
        toAttributeValueResolver = new MapToAttributeValueResolver();
        environment = Environment.getInstance();
    }

    public void setDataSource(Map<String, String> dataSource) {
        attributeValues.clear();
        pathAttributeValues.clear();
        this.dataSource = dataSource;
        parseAttributeValues();
    }

    protected void parseAttributeValues(){
        AttributeValueWrapper wrapper = toAttributeValueResolver.parse(getDataSource());
        attributeValues.putAll(wrapper.getAttributeValueMap());
        for (AttributeValue attributeValue : wrapper.getGlobalCache().values()) {
            pathAttributeValues.put(attributeValue.getPath(), attributeValue);
        }
    }

    public IoLog getLog(){
        return getEnvironment().getLog();
    }

    public void setAttributeSeparator(String attributeSeparator) {
        this.attributeSeparator = attributeSeparator;
        toAttributeValueResolver.setElementSeparator(attributeSeparator);
    }

    @Override
    public String selectAttribute(String name) {
        check(dataSource);
        return dataSource.get(name);
    }

    @Override
    public AttributeValue selectAttributeValue(String path) {
        AttributeValue attributeValue = pathAttributeValues.get(path);
        Assert.notNull(attributeValue, "can not find attribute of path: " + path);
        return attributeValue;
    }

    protected void check(Map<String, String> source){
        if (source == null){
            throw new IllegalStateException("Cannot query an empty data source");
        }
    }

    @Override
    public Map<String, AttributeValue> selectGroupAttributes(Map<String, String> source, String command, boolean removePrefix) {
        check(source);
        if (!StringUtils.hasText(command)) {
            return pathAttributeValues;
        }

        command = StringUtils.addIfNotEndWith(command, ".");
        Map<String, AttributeValue> result = new HashMap<>();
        for (String key : pathAttributeValues.keySet()) {
            if (key.startsWith(command)){
                result.put(removePrefix ? StringUtils.removeIfStartWith(key, command)
                        : key, pathAttributeValues.get(key));
            }
        }
        return result;
    }

    @Override
    public Map<String, String> selectGroupSources(Map<String, String> source, String command, boolean removePrefix) {
        check(source);
        if (!StringUtils.hasText(command)) {
            return source;
        }

        command = StringUtils.addIfNotEndWith(command, ".");
        Map<String, String> result = new HashMap<>();
        for (String key : source.keySet()) {
            if (key.startsWith(command)){
                result.put(removePrefix ? StringUtils.removeIfStartWith(key, command)
                        : key, source.get(key));
            }
        }
        return result;
    }
}
