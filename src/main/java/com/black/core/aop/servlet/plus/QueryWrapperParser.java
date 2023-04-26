package com.black.core.aop.servlet.plus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.tools.BaseBean;
import com.black.utils.ReflexHandler;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@Log4j2
public class QueryWrapperParser extends AbstractWrapperParser {



    protected QueryWrapperParser(MappingPolicyHandler policyHandler) {
        super(policyHandler);
    }

    @Override
    public Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper,
                            List<?> listArg, Class<?> genericType,
                            Class<?> entity, Annotation wrapperAnnotation,
                            PlusMethodWrapper methodWrapper, boolean parsed) {
        if (listArg.isEmpty()){
            return wrapper;
        }
        WriedQueryWrapper queryWrapper = (WriedQueryWrapper) wrapperAnnotation;
        //拿到一个元素
        MappingPolicy policy = getPolicy(entity);
        if (genericType.equals(Object.class)){

            //说明这个集合的泛型十分模糊, 可能是一个 JSONArray
            //所有的情况, 元素内部是一个 json, 可以转成实体类, 或者是一组直接数据
            //这里主要考虑为json的可能性
            //最终转换成一个 list<entity> 的数组
            List<Object> afterConvertList = new ArrayList<>();
            boolean successfulConvert = true;
            for (Object arg : listArg) {
                Class<?> argClass = arg.getClass();
                if (entity.isAssignableFrom(argClass)){
                    afterConvertList.add(arg);
                }else if (String.class.isAssignableFrom(argClass)){
                    doInvoker(queryWrapper, wrapper, policy, listArg, entity);
                    break;
                }else {
                    try {
                        JSONObject jsonObject = (JSONObject) JSON.toJSON(arg);
                        afterConvertList.add(JsonUtils.toObject(jsonObject, ReflexUtils.instance(entity)));
                    }catch (Exception e){

                        //无法转成实体类, 则证明是一组直接数据
                        successfulConvert = false;
                        break;
                    }
                }
            }

            if (!successfulConvert){
                doInvoker(queryWrapper, wrapper, policy, listArg, entity);
            }else {
                //成功转换
                doInvokerWithList(queryWrapper, wrapper, policy, afterConvertList);
            }

        } else if (String.class.isAssignableFrom(genericType)){
               doInvoker(queryWrapper, wrapper, policy, listArg, entity);
        }else {
            if (log.isWarnEnabled()) {
                log.warn("the type parameter cannot be resolved：{}", genericType);
            }
        }
        return wrapper;
    }

    protected void doInvokerWithList(WriedQueryWrapper queryWrapper, AbstractWrapper<?, String, ?> wrapper,
                                     MappingPolicy policy, List<?> listArg){

        for (int i = 0; i < listArg.size(); i++) {
            Object o = listArg.get(i);
            if (i == 0){

                for (Field field : ReflexHandler.getAccessibleFields(o)) {
                    String name = field.getName();
                    Object value = ReflexUtils.getValue(field, o);
                    if (value != null){
                        wrapper.eq(policyHandler.handlerByPolicy(name, policy), value);
                    }
                }
            }else {
                wrapper.or(w -> {for (Field field : ReflexHandler.getAccessibleFields(o)) {
                    String name = field.getName();
                    Object value = ReflexUtils.getValue(field, o);
                    if (value != null){
                        wrapper.eq(policyHandler.handlerByPolicy(name, policy), value);
                    }

                }});
            }
        }
    }

    protected void doInvoker(WriedQueryWrapper queryWrapper, AbstractWrapper<?, String, ?> wrapper,
                             MappingPolicy policy, List<?> listArg, Class<?> entity){
        //try to convert json
        String operator = queryWrapper.ifArrayOperator();
        //获取该值对应的字段名
        String name = queryWrapper.ifArrayPointFieldName();
        if ("".equals(name)){
            name = getKey(entity);
        }
        if (IN_OPERATOR.equals(operator)){
            wrapper.in(policyHandler.handlerByPolicy(name, policy), listArg);
        }else {
            wrapper.notIn(policyHandler.handlerByPolicy(name, policy), listArg);
        }
    }

    @Override
    public Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper,
                            Map<String, Object> mapArg,
                            Class<?> entity, Annotation wrapperAnnotation,
                            PlusMethodWrapper methodWrapper) {
        return parse(wrapper, JsonUtils.toObject(new JSONObject(mapArg),
                ReflexUtils.instance(entity)), entity, wrapperAnnotation, methodWrapper);
    }

    @Override
    public Wrapper<?> parse(AbstractWrapper<?, String, ?> wrapper, Object instance,
                            Class<?> entity, Annotation wrapperAnnotation,
                            PlusMethodWrapper methodWrapper) {
        doParseMapArg(instance, entity, wrapper, (WriedQueryWrapper) wrapperAnnotation);
        return wrapper;
    }


    protected void doParseMapArg(Object instance, Class<?> entity, AbstractWrapper<?, String, ?>  wrapper, WriedQueryWrapper annotation){
        //执行填充defalult 方法
        if (annotation.ifBaseBeanInvokeWriedDefaultValue()){
            if (instance instanceof BaseBean){
                BaseBean<?> baseBean = (BaseBean<?>) instance;
                baseBean.wriedValue();
            }
        }
        //转成实体类
        MappingPolicy policy = getPolicy(instance);

        //存放 like 操作字段的数组
        HashSet<String> likeSet = new HashSet<>(Arrays.asList(annotation.likeConditionFields()));
        //存放 or 操作字段的数组
        HashSet<String> orSet = new HashSet<>(Arrays.asList(annotation.orConditionFields()));
        Map<String, Object> orValues = new HashMap<>(orSet.size());
        //遍历所有的字段
        for (Field field : ReflexHandler.getAccessibleFields(instance)) {
            String name = field.getName();
            Object value = ReflexUtils.getValue(field, instance);
            if (value != null){

                if (likeSet.contains(name)){
                    wrapper.like(policyHandler.handlerByPolicy(name, policy), value);
                }else if (orSet.contains(name)){
                    orValues.put(name, value);
                }else {
                    wrapper.eq(policyHandler.handlerByPolicy(name, policy), value);
                }

            }else {
                //如果不忽略空
                if (!annotation.ignoreNullValue()) {
                    wrapper.isNull(policyHandler.handlerByPolicy(name, policy));
                }
            }
        }

        //最后拼接 or 操作符
        if (!orValues.isEmpty()){
            wrapper.or(i -> orValues.forEach((name, val) ->{
                if (likeSet.contains(name)){
                    wrapper.like(policyHandler.handlerByPolicy(name, policy), val);
                }else {
                    wrapper.eq(policyHandler.handlerByPolicy(name, policy), val);
                }
            }));
        }

        //finish
    }

}
