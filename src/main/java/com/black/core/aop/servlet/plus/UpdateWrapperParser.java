package com.black.core.aop.servlet.plus;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.core.builder.Col;
import com.black.core.json.ReflexUtils;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflexHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class UpdateWrapperParser extends QueryWrapperParser {

    protected UpdateWrapperParser(MappingPolicyHandler policyHandler) {
        super(policyHandler);
    }

    public UpdateWrapper<?> parseArray(List<?> array, Class<?> entity, Annotation wrapperAnnotation,
                                       PlusMethodWrapper methodWrapper){

        WriedUpdateWrapper updateWrapper = (WriedUpdateWrapper) wrapperAnnotation;
        WriedQueryWrapper queryWrapperAnnotation = updateWrapper.queryWrapper();
        UpdateWrapper<Object> wrapper = new UpdateWrapper<>();
        MappingPolicy policy = getPolicy(entity);
        Map<String, String> afterHandlerFill = handlerFillSet(new HashSet<>(Col.m(updateWrapper.autoInjection())));
        Set<String> setSet = new HashSet<>(Col.m(updateWrapper.setFields(), afterHandlerFill.keySet().toArray(new String[0])));
        doHandlerSet(wrapper, setSet, afterHandlerFill, entity, policy);
        return wrapper;
    }

    public UpdateWrapper<?> parseUpdateWrapper(Object instance, Class<?> entity,
                                               Annotation wrapperAnnotation, PlusMethodWrapper methodWrapper){
        WriedUpdateWrapper updateWrapper = (WriedUpdateWrapper) wrapperAnnotation;
        UpdateWrapper<Object> wrapper = new UpdateWrapper<>();

        hasRequiredProperties(updateWrapper.requiredProperties(), instance);

        //默认作为条件的字段
        String[] condition = updateWrapper.condition();
        if (condition.length == 0){
            condition = new String[1];
            condition[0] = getKey(entity);
        }
        parse(wrapper, BeanUtil.coryBean(instance, condition), entity, updateWrapper.queryWrapper(), methodWrapper);

        //参数类型
        MappingPolicy policy = getPolicy(instance);
        Map<String, String> afterHandlerFill = handlerFillSet(new HashSet<>(Col.m(updateWrapper.autoInjection())));
        Set<String> setSet = new HashSet<>(Col.m(updateWrapper.setFields(), afterHandlerFill.keySet().toArray(new String[0])));
        for (Field field : ReflexHandler.getAccessibleFields(instance)) {
            String name = field.getName();
            if (setSet.isEmpty() || setSet.contains(name)){
                if (afterHandlerFill.containsKey(name)){
                    //fill attribute

                    String val = afterHandlerFill.get(name);
                    if (val != null){
                        try {
                            BeanUtil.setDefaultValue(instance, field, val);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        BeanUtil.fill(instance, field);
                    }
                }
            }
            Object value = ReflexUtils.getValue(field, instance);
            if (value != null){
                wrapper.set(policyHandler.handlerByPolicy(name, policy), value);
            }
        }
        return wrapper;
    }


    protected void doHandlerSet(UpdateWrapper<?> updateWrapper,
                                Set<String> setSet,
                                Map<String, String> afterHandlerFill,
                                Class<?> entity,
                                MappingPolicy policy){
        for (String column : setSet) {
            if (setSet.isEmpty() || setSet.contains(column)){
                if (afterHandlerFill.containsKey(column)){
                    //fill attribute

                    String val = afterHandlerFill.get(column);
                    Object convertResult;
                    Class<?> type = ReflexUtils.getField(column, entity).getType();
                    if (val != null){
                        convertResult = BeanUtil.getDefaultValue(type, val);
                    }else {
                        convertResult = BeanUtil.getTimeValue(type);
                    }
                    updateWrapper.set(policyHandler.handlerByPolicy(column, policy), convertResult);
                }
            }
        }
    }

    protected boolean elementIsJson(List<?> array){
        if (array.isEmpty()){
            return false;
        }
        Object o = array.get(0);
        if (o == null){
            return false;
        }
        try {
            Object json = JSON.toJSON(o);
            return true;
        }catch (JSONException e){
            return false;
        }
    }

    protected void hasRequiredProperties(String[] properties, List<?> array, boolean convert){
        if (!convert){
            if (array.isEmpty() && properties.length != 0){
                throw new RuntimeException("缺少:" + Arrays.toString(properties));
            }
        }else {
            for (String property : properties) {
                for (Object instance : array) {
                    try {

                        Field field = ReflexUtils.getField(property, instance);
                        if (ReflexUtils.getValue(field, instance) == null) {
                            throw new RuntimeException("缺少:" + property);
                        }
                    }catch (RuntimeException e){
                        //no field
                    }
                }
            }
        }

    }

    protected void hasRequiredProperties(String[] properties, Object instance){
        for (String property : properties) {
            try {

                Field field = ReflexUtils.getField(property, instance);
                if (ReflexUtils.getValue(field, instance) == null) {
                    throw new RuntimeException("缺少:" + property);
                }
            }catch (RuntimeException e){
                //no field
            }
        }
    }

    protected Map<String, String> handlerFillSet(Set<String> fillSet){
        Map<String, String> result = new HashMap<>();
        for (String set : fillSet) {
            String[] vs = set.split(":");
            if (vs.length == 1){
                result.put(vs[0], null);
            }else {
                result.put(vs[0].trim(), vs[1].trim());
            }
        }
        return result;
    }
}
