package com.black.core.aop.servlet.plus.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.black.core.aop.servlet.plus.*;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;
import com.black.core.json.JsonUtils;
import com.black.core.tools.BaseBean;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWrapperHandler implements PlusVariableResolver {


    public final MappingPolicyHandler policyHandler;

    public AbstractWrapperHandler(MappingPolicyHandler policyHandler) {
        this.policyHandler = policyHandler;
    }

    protected void handlerObject(Object arg,
                                 AbstractWrapper<?, String, ?> wrapper,
                                 EntryWrapper entryWrapper,
                                 PlusMethodWrapper plusMethodWrapper,
                                 QueryWrapperConfiguration configuration,
                                 Set<String> includes){
        Class<?> targetClazz = entryWrapper.getTargetClazz();
        //在拿到参数类型
        Class<?> argClass = arg.getClass();
        if (argClass.equals(targetClazz)){
            handlerEnrty(arg, wrapper, entryWrapper, plusMethodWrapper, configuration, includes);
        }else if (String.class.equals(argClass)){
            handlerString((String) arg, wrapper, entryWrapper, plusMethodWrapper, configuration, includes);
        }else if (Map.class.isAssignableFrom(argClass)){
            handlerJson(new JSONObject((Map<String, Object>) arg), wrapper, entryWrapper, plusMethodWrapper, configuration, includes);
        }else if (List.class.isAssignableFrom(argClass)){
            handlerArray((List<?>) arg, wrapper, entryWrapper, configuration);
        }else if (Object.class.equals(argClass)){
            throw new RuntimeException("参数必须明确, 不能用 Object 涵盖");
        }else {
            throw new RuntimeException("无法处理的参数类型:" + argClass);
        }
    }

    protected void handlerEnrty(Object entry,
                                AbstractWrapper<?, String, ?> wrapper,
                                EntryWrapper entryWrapper,
                                PlusMethodWrapper plusMethodWrapper,
                                QueryWrapperConfiguration configuration,
                                Set<String> includes){

        JSONObject json = JsonUtils.toJson(entry);
        //设置数据源
        entryWrapper.setSource(entry, json);
        handlerJson(json, wrapper, entryWrapper, plusMethodWrapper, configuration, includes);
    }


    protected void handlerString(String strSource,
                                 AbstractWrapper<?, String, ?> wrapper,
                                 EntryWrapper entryWrapper,
                                 PlusMethodWrapper plusMethodWrapper,
                                 QueryWrapperConfiguration configuration,
                                 Set<String> includes){
        try {
            JSONObject parse = JSON.parseObject(strSource);
            handlerJson(parse, wrapper, entryWrapper, plusMethodWrapper, configuration, includes);
        }catch (JSONException parseJsonError){

            //无法转成 json
            try {
                JSONArray array = JSON.parseArray(strSource);
                handlerArray(array, wrapper, entryWrapper, configuration);
            }catch (JSONException parseArrayError){
                //无法转成 array
                throw new RuntimeException("数据源无法转成 array 或者 json");
            }
        }
    }

    protected void handlerJson(JSONObject json,
                               AbstractWrapper<?, String, ?> wrapper,
                               EntryWrapper entryWrapper,
                               PlusMethodWrapper plusMethodWrapper,
                               QueryWrapperConfiguration configuration,
                               Set<String> includes){

        //先处理实体类bean
        Object source = entryWrapper.getSource();
        if (source == null){
            //如果实体类数据源为空, 那么转换
            entryWrapper.setSource(JsonUtils.toObject(json, entryWrapper.getTargetClazz()), json);
            source = entryWrapper.getSource();
        }

        if (source instanceof BaseBean){
            if (configuration.isIfBaseBeanInvokeWriedDefaultValue()) {
                BaseBean<?> baseBean = (BaseBean<?>) source;
                baseBean.wriedValue();
                json.putAll(JsonUtils.toJson(baseBean));
            }
        }

        //在处理 baseBean 结束以后再构造数据源
        //构造数据源
        entryWrapper.putAll(json);

        //处理数据源
        handlerFinalCondition(entryWrapper, wrapper);

        //处理 and 连接符
        Map<String, Object> dynamicArgs = entryWrapper.getDynamicArgs();
        doHandler(wrapper, dynamicArgs, entryWrapper, configuration, true, includes);

        //处理 or 连接符
        Map<String, Object> orDynamicArgs = entryWrapper.getOrDynamicArgs();
        doHandler(wrapper, orDynamicArgs, entryWrapper, configuration, false, includes);

        //处理排序
        handlerOrder(wrapper, entryWrapper, configuration);

        //最后拼接sql
        handlerApplySql(wrapper, entryWrapper, configuration);
    }


    protected void handlerFinalCondition(EntryWrapper entryWrapper,
                                         AbstractWrapper<?, String, ?> wrapper){
        entryWrapper.initTotalSource();
        Map<String, Object> totalSource = entryWrapper.getTotalSource();
        Map<String, String> finalSource = entryWrapper.getFinalSource();
        Set<String> orSet = entryWrapper.getOrSet();
        Map<String, Object> processor = MethodEntryExecutor.processor(finalSource, entryWrapper, totalSource);
        entryWrapper.putAll(processor);
    }

    protected void handlerApplySql(AbstractWrapper<?, String, ?> wrapper,
                                   EntryWrapper entryWrapper,
                                   QueryWrapperConfiguration configuration){
        String applySql = configuration.getApplySql();
        if (StringUtils.hasText(applySql)){
            wrapper.last(applySql);
        }
    }

    protected void handlerOrder(AbstractWrapper<?, String, ?> wrapper,
                                EntryWrapper entryWrapper,
                                QueryWrapperConfiguration configuration){
        String[] orderByAsc = configuration.getOrderByAsc();
        Set<String> keySet = entryWrapper.getFields().keySet();
        MappingPolicy policy = entryWrapper.getPolicy();
        if (orderByAsc != null && orderByAsc.length != 0){
            for (String name : orderByAsc) {
                if (keySet.contains(name)){
                    String column = policyHandler.handlerByPolicy(name, policy);
                    wrapper.orderByAsc(column);
                }
            }
        }
        String[] orderByDesc = configuration.getOrderByDesc();
        if (orderByDesc != null && orderByDesc.length != 0){
            for (String name : orderByDesc) {
                if (keySet.contains(name)){
                    String column = policyHandler.handlerByPolicy(name, policy);
                    wrapper.orderByDesc(column);
                }
            }
        }
    }

    protected void doHandler(AbstractWrapper<?, String, ?> wrapper,
                             Map<String, Object> source,
                             EntryWrapper entryWrapper,
                             QueryWrapperConfiguration configuration, boolean and, Set<String> includes){
        boolean annotationIgnore = true;
        boolean ignore = true;

        Set<String> limits =  entryWrapper.getFields().keySet();
        Set<String> annotationExclusions = configuration.getAnnotationExclusions(limits);
        for (String alias : source.keySet()) {
            if (limits.contains(alias)){
                ignore = false;
            }
            if (annotationExclusions.contains(alias)){
                annotationIgnore = false;
            }
        }
        if (source.isEmpty() || ignore || annotationIgnore){
            return;
        }

        if (and){
            wrapper.and(i ->{
                source.forEach((k ,v) ->{
                    if (annotationExclusions.contains(k)){
                        if (includes == null || includes.isEmpty() || includes.contains(k)) {
                            this.doFillValue(i, k, entryWrapper, v, configuration);
                        }
                    }
                });
            });

        }else {
            wrapper.or(i ->{
                source.forEach((k ,v) ->{
                    if (annotationExclusions.contains(k)){
                        if (includes == null || includes.isEmpty() || includes.contains(k)) {
                            this.doFillValue(i, k, entryWrapper, v, configuration);
                        }
                    }
                });
            });
        }

    }

    protected void doFillValue(AbstractWrapper<?, String, ?> wrapper, String name, EntryWrapper entryWrapper,
                               Object value, QueryWrapperConfiguration configuration){
        //name 必须存在于
        Set<String> limits = entryWrapper.getFields().keySet();
        //要填充的属性, 必须存在于实体类字段name中
        if (!limits.contains(name)){
            return;
        }
        Set<String> likeSet = entryWrapper.getLikeSet();
        MappingPolicy policy = entryWrapper.getPolicy();
        boolean ignoreNullValue = configuration.isIgnoreNullValue();
        String column = policyHandler.handlerByPolicy(name, policy);
        if (likeSet.contains(name)) {
            if (value != null || !ignoreNullValue){
                wrapper.like(column, value);
            }

        }else {
            if (value != null || !ignoreNullValue){
                wrapper.eq(column, value);
            }
        }
    }


    //这里就是处理 array
    protected void handlerArray(List<?> list,
                                AbstractWrapper<?, String, ?> wrapper,
                                EntryWrapper entryWrapper,
                                QueryWrapperConfiguration configuration){
        final MappingPolicy policy = entryWrapper.getPolicy();

        String ifArrayOperator = configuration.getIfArrayOperator();
        //拿到该array对应的字段名
        String fieldName = ifNullIsAndId(configuration.getIfArrayPointFieldName(), entryWrapper);
        String columnName = policyHandler.handlerByPolicy(fieldName, policy);
        if ("in".equals(ifArrayOperator)){
            wrapper.in(columnName, list);
        }else {
            wrapper.notIn(columnName, list);
        }
    }

    protected String ifNullIsAndId(String name, EntryWrapper entryWrapper){
        if (!StringUtils.hasText(name)) {
            return entryWrapper.getPrimaryKey();
        }
        return name;
    }
}
