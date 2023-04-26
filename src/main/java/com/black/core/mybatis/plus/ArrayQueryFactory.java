package com.black.core.mybatis.plus;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.core.aop.servlet.plus.MappingPolicy;
import com.black.core.aop.servlet.plus.MappingPolicyHandler;
import com.black.core.chain.GroupUtils;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ArrayQueryFactory {

    private final Collection<Configuration> configurations = new HashSet<>();
    private final MappingPolicyHandler policyHandler;

    public ArrayQueryFactory() {
        policyHandler = new MappingPolicyHandler();
    }

    public void add(Configuration configuration){
        if (configuration != null){
            configurations.add(configuration);
        }
    }

    public void addAll(Collection<Configuration> configurations){
        if (configurations != null){
            this.configurations.addAll(configurations);
        }
    }

    public Collection<Map<String, Object>> openQuery(Collection<?> source){
        return openQuery(source, false);
    }

    public Collection<Map<String, Object>> openQuery(Collection<?> source, boolean toSupper){
        if (source == null || source.isEmpty()){
            return new ArrayList<>();
        }
        List<Map<String, Object>> mapList = source.stream().map(element -> {
            Map<String, Object> map;
            if (element instanceof Map) {
                map = (Map<String, Object>) element;
            } else {
                map = JsonUtils.toJson(element, true, toSupper);
            }
            return map;
        }).collect(Collectors.toList());
        return doQuery(mapList);
    }

    public Collection<Map<String, Object>> doQuery(Collection<Map<String, Object>> source){
        for (Configuration configuration : configurations) {
            final Map<String, String> mapping = configuration.getMapping();
            final String groupByKey = configuration.getGroupBy();
            final String mapKey = configuration.getMapKey();
            Collection<?> partResult = doInvoke(configuration, source);
            Map<String, ? extends List<?>> partList = GroupUtils.groupList(partResult, element -> {
                if (element instanceof Map) {
                    return ((Map<String, Object>) element).get(groupByKey).toString();
                } else {
                    return (String) ReflexUtils.getValue(ReflexUtils.getField(groupByKey, element), element);
                }
            });
            for (Map<String, Object> pojo : source) {
                String o = (String) pojo.get(mapping.get(groupByKey));
                List<?> list = partList.get(o);
                pojo.put(mapKey, list);
            }
        }
        return source;
    }


    protected <T> Collection<T> doInvoke(Configuration configuration, Collection<Map<String, Object>> source){
        AbstractWrapper<T, String, ?> wrapper = new QueryWrapper<>();

        try {

            Set<String> and = configuration.getAnd();
            if (!and.isEmpty()){
                wrapper.and(i -> {
                    doFillWrapper(configuration.isIn(), i, and, configuration, source);
                });
            }

            Set<String> or = configuration.getOr();
            if (!or.isEmpty()){
                wrapper.or(i -> {
                    doFillWrapper(configuration.isIn(), i, or, configuration, source);
                });
            }

            Map<String, ConditionValue> conditionMap = configuration.getConditionMap();
            handlerConditionMap(conditionMap, wrapper, configuration);

            //处理排序
            Set<String> orderByAsc = configuration.getOrderByAsc();
            handlerOrder(true, wrapper, orderByAsc, configuration);

            Set<String> orderByDesc = configuration.getOrderByDesc();
            handlerOrder(false, wrapper, orderByDesc, configuration);

            BaseMapper<T> mapper = (BaseMapper<T>) configuration.getMapper();
            return mapper.selectList(wrapper);
        }catch (RuntimeException ex){
            throw new ArrayQueryException(ex);
        }
    }


    protected void doFillWrapper(boolean in, AbstractWrapper<?, String, ?> wrapper,
                                 Set<String> field, Configuration configuration, Collection<Map<String, Object>> source){
        final Map<String, String> mapping = configuration.getMapping();
        final Set<String> fieldNames = configuration.getFieldNames();
        for (String name : field) {

            //如果指定的字段在实体类中不存在, 那么不会注入
            if (!fieldNames.contains(name)){
                continue;
            }

            //存在映射
            if (mapping.containsKey(name)){
                String mappingName = mapping.get(name);

                //通过映射在结果中拿到所有值
                List<Object> mappingResult = source
                        .stream()
                        .map(s -> {
                            return s.get(mappingName);
                        }).collect(Collectors.toList());

                String column = handlerPolicy(name, configuration);
                if (in){
                    wrapper.in(column, mappingResult);
                }else {
                    wrapper.notIn(column, mappingResult);
                }
            }
        }
    }

    protected void handlerConditionMap(Map<String, ConditionValue> conditionMap,
                                       AbstractWrapper<?, String, ?> wrapper,
                                       Configuration configuration){
        Set<String> fieldNames = configuration.getFieldNames();
        conditionMap.forEach((name, cv) ->{
            if (fieldNames.contains(name)){
                String column = handlerPolicy(name, configuration);
                if (cv.and){
                    wrapper.and(o ->{
                        if (cv.like){
                            o.like(column, cv.entryValue);
                        }else {
                            o.eq(column, cv.entryValue);
                        }
                    });
                }else {
                    wrapper.or(o ->{
                        if (cv.like){
                            o.like(column, cv.entryValue);
                        }else {
                            o.eq(column, cv.entryValue);
                        }
                    });
                }
            }
        });
    }


    protected void handlerOrder(boolean asc, AbstractWrapper<?, String, ?> wrapper, Set<String> names, Configuration configuration){
        Set<String> fieldNames = configuration.getFieldNames();
        for (String name : names) {
            if (fieldNames.contains(name)){
                String column = handlerPolicy(name, configuration);
                if (asc){
                    wrapper.orderByAsc(column);
                }else {
                    wrapper.orderByDesc(column);
                }
            }
        }
    }

    protected String handlerPolicy(String name, Configuration configuration){
        MappingPolicy policy = configuration.getPolicy();
        return policyHandler.handlerByPolicy(name, policy);
    }
}
