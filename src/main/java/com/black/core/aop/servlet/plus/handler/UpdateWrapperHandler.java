package com.black.core.aop.servlet.plus.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.core.aop.servlet.plus.*;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;
import com.black.core.builder.Col;
import com.black.core.tools.BeanUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UpdateWrapperHandler extends AbstractWrapperHandler {

    public UpdateWrapperHandler(MappingPolicyHandler policyHandler) {
        super(policyHandler);
    }

    @Override
    public boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper) {
        return methodWrapper.getAnnotationMap().containsKey(WriedUpdateWrapper.class);
    }

    @Override
    public Wrapper<?> handler(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper,
                              QueryWrapperConfiguration configuration) {
        UpdateWrapper<?> updateWrapper = new UpdateWrapper<>();

        //首先拿到注解
        WriedUpdateWrapper wrapperAnnotation = (WriedUpdateWrapper) methodWrapper.getAnnotationMap().get(WriedUpdateWrapper.class);
        String[] condition = wrapperAnnotation.condition();
        //首先处理 where 的数据
        handlerObject(entryWrapper.getArg(), updateWrapper, entryWrapper, methodWrapper, configuration, new HashSet<>(Col.as(condition)));

        //然后处理update的set
        Map<String, Object> primordialArgs = entryWrapper.getPrimordialArgs();

        //这些是参数中必须存在的key
        String[] properties = wrapperAnnotation.requiredProperties();
        for (String property : properties) {
            if (!primordialArgs.containsKey(property)){
                throw new RuntimeException("参数缺少:" + property);
            }
        }

        Map<String, String> afterHandlerFill = handlerFillSet(new HashSet<>(Col.m(wrapperAnnotation.autoInjection())));
        //获取更新需要的所有set的字段名
        Set<String> setSet = getSets(wrapperAnnotation, afterHandlerFill, entryWrapper);
        MappingPolicy policy = entryWrapper.getPolicy();
        for (String set : setSet) {
            Object value;

            //如果该字段需要自动注入 ....
            if (afterHandlerFill.containsKey(set)){
                String val = afterHandlerFill.get(set);
                Class<?> type = getType(entryWrapper, set);
                if (val != null){
                    value = BeanUtil.getDefaultValue(type, val);
                }else {
                    value = BeanUtil.getTimeValue(type);
                }
            }else {

                //该字段不需要自动注入 ....
                value = entryWrapper.getPrimordialValue(set);
            }
            if (value != null){
                String column = policyHandler.handlerByPolicy(set, policy);
                updateWrapper.set(column, value);
            }
        }
        return updateWrapper;
    }


    protected Class<?> getType(EntryWrapper entryWrapper, String name){
        Field field = entryWrapper.getFields().get(name);
        if (field == null){
            throw new RuntimeException("无法找到字段:" + name);
        }
        return field.getType();
    }

    protected Set<String> getSets(WriedUpdateWrapper wrapperAnnotation,
                                  Map<String, String> afterHandlerFill,
                                  EntryWrapper entryWrapper){
        Set<String> result = new HashSet<>(afterHandlerFill.keySet());
        String[] fields = wrapperAnnotation.setFields();
        Set<String> argNames = entryWrapper.getPrimordialArgNames();
        if (fields.length == 1){
            String field = fields[0];
            if ("*".equals(field)){
                result.addAll(argNames);
                return result;
            }
        }
        result.addAll(Col.as(wrapperAnnotation.setFields()));
        return result;
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
