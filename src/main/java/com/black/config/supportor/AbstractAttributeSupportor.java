package com.black.config.supportor;

import com.alibaba.fastjson.JSONObject;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.config.*;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.JsonUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StringUtils;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import javafx.print.Collation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all")
public abstract class AbstractAttributeSupportor implements AttributeInjectorSupportor {


    protected void setValue(FieldWrapper fw,
                            String name,
                            Object bean,
                            AttributeValue attributeValue,
                            ConfiguringAttributeAutoinjector autoinjector){
        Class<?> type = fw.getType();
        Object value = null;
        if (attributeValue.isMap()){
            value = selectByType(name, fw, bean, autoinjector);
        }else {
            value = getAttributeObjectValue(attributeValue);
            if (!StringUtils.hasText(value.toString())){
                value = null;
            }
        }
        MethodWrapper method = SetGetUtils.getSetMethod(fw.get());
        if (method != null){
            method.invoke(bean, checkArg(value, type, fw));
        }
    }
    protected Object getAttributeStringValue(AttributeValue attributeValue){
        Object objectValue = getAttributeObjectValue(attributeValue);
        return objectValue == null ? null : objectValue.toString();
    }

    protected Object getAttributeObjectValue(AttributeValue attributeValue){
        if (attributeValue.isMap()){
            throw new IllegalStateException("get attribute value, but is map");
        }
        Object value = null;
        if (attributeValue.isValueMap()) {
            value = attributeValue.getJsonObject();
        }else
        if (attributeValue.isValueList()){
            value = attributeValue.getJsonArray();
        }else {
            value = attributeValue.getValue();
        }
        return value;
    }

    protected Object checkArg(Object arg, Class<?> type, FieldWrapper fw){
        if (arg == null){
            return arg;
        }
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(arg);
        if (type.isAssignableFrom(primordialClass)){
            return arg;
        }

        if (arg instanceof Map){
            if (type.equals(String.class)){
                return arg.toString();
            }

            Object instance = ReflectionUtils.instance(type);
            BeanUtil.mapping(instance, (Map<String, Object>) arg);
            return instance;
        }

        if (arg instanceof Collation){
            if (type.equals(String.class)){
                return arg.toString();
            }

            Class<?>[] genericVals = ReflectionUtils.genericVal(fw.getField(), type);
            Class<?> genericVal = genericVals[0];
            Collection<Object> collection = ServiceUtils.createCollection(type);
            Collection<?> c = (Collection<?>) arg;
            for (Object o : c) {
                Object instance = ReflectionUtils.instance(genericVal);
                JSONObject json = JsonUtils.letJson(o);
                BeanUtil.mapping(instance, json);
                collection.add(c);
            }
            return collection;
        }
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        return typeHandler.convert(type, arg);
    }

    protected Object selectByType(String name,
                                  FieldWrapper fw,
                                  Object bean,
                                  ConfiguringAttributeAutoinjector autoinjector){
        Class<?> type = fw.getType();
        Object fieldValue = fw.getValue(bean);
        if (Map.class.isAssignableFrom(type)){
            Class<?>[] genericVals = ReflectionUtils.genericVal(fw.getField(), type);
            if (genericVals.length != 2){
                throw new ConfigurerException("For map type properties, a generic type must be indicated");
            }
            if (!genericVals[0].equals(String.class)) {
                throw new ConfigurerException("For a map type field, the first generic type must be string");
            }

            Class<?> genericVal = genericVals[1];
            Map<String, Object> map = fieldValue == null ? new LinkedHashMap<>() : (Map<String, Object>) fieldValue;
            AttributeValue attribute = autoinjector.selectAttributeValue(name);
            if (!attribute.isMap()){
                if (attribute.isValueMap()){
                    return attribute.getJsonObject();
                }else {
                    throw new ConfigurerException("Unable to inject into map field z");
                }
            }
            Map<String, AttributeValue> mapChilds = attribute.getMapChilds();
            for (String alias : mapChilds.keySet()) {
                AttributeValue value = mapChilds.get(alias);
                Object poured = pour0(genericVal, value, null, autoinjector);
                map.put(alias, poured);
            }
            return map;
        }else if (Collection.class.isAssignableFrom(type)){
            Class<?>[] genericVals = ReflectionUtils.genericVal(fw.getField(), type);
            Class<?> genericVal = genericVals[0];
            Collection<Object> collection = fieldValue == null ?
                    ServiceUtils.createCollection(type) : (Collection<Object>) fieldValue;
            AttributeValue attribute = autoinjector.selectAttributeValue(name);
            Map<String, AttributeValue> mapChilds = attribute.getMapChilds();
            for (AttributeValue attributeValue : mapChilds.values()) {
                Object poured = pour0(genericVal, attributeValue, null, autoinjector);
                collection.add(poured);
            }
            return collection;
        }else {
            AttributeValue attributeValue = autoinjector.selectAttributeValue(name);
            return pour0(type, attributeValue, fieldValue, autoinjector);
        }
    }

    protected Object pour0(Class<?> type, AttributeValue value, Object instance,
                           ConfiguringAttributeAutoinjector autoinjector){
        String name = type.getName();
        if (ClassWrapper.isBasic(name) || ClassWrapper.isBasicWrapper(name)){
            Object stringValue = getAttributeStringValue(value);
            return TypeConvertCache.initAndGet().convert(type, stringValue);
        }

        if (type.equals(String.class)){
            return getAttributeStringValue(value);
        }
        Map<String, String> source = value.toSource(false);
        if (instance == null){
            instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        }
        ConfiguringAttributeAutoinjector newAutoInjector = autoinjector.copy();
        newAutoInjector.setDataSource(source);
        newAutoInjector.pourintoBean(instance);
        return instance;
    }


}
