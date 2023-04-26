package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.aop.servlet.item.annotation.LAExpression;
import com.black.core.aop.servlet.item.annotation.LASupportedMethod;
import com.black.core.aop.servlet.item.annotation.OperatorProcessor;
import com.black.core.chain.*;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.json.Trust;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@GlobalAround  @ChainClient
public class ItemAroundHandler implements GlobalAroundResolver, CollectedCilent {

    private final InstanceFactory instanceFactory;
    private ThreadLocalItemResolver itemResolver;
    private final ThreadLocal<Map<Method, Map<String, Object>>> sourceCache = new ThreadLocal<>();
    public ItemAroundHandler(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    public Object[] processorLA(Object[] args, HttpMethodWrapper httpMethodWrapper){
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        if (!wrapper.hasAnnotation(LASupportedMethod.class)){
            return args;
        }
        LASupportedMethod laSupportedMethod = wrapper.getAnnotation(LASupportedMethod.class);

        //整合全局变量
        Map<String, Object> source = collectParamterSource(httpMethodWrapper, args);
        source.putAll(parseDefineVariables(laSupportedMethod.defineVariables()));

        List<ParameterWrapper> parameterByAnnotation = httpMethodWrapper.getParameterByAnnotation(LAExpression.class);
        if (!CollectionUtils.isEmpty(parameterByAnnotation)){
            for (ParameterWrapper parameterWrapper : parameterByAnnotation) {
                LAExpression laExpression = AnnotationUtils.getAnnotation(parameterWrapper.getParameter(), LAExpression.class);
                Object value = null;
                String name = parameterWrapper.getName();
                for (String item : laExpression.value()) {
                    value = itemResolver.resolver(source, item);
                    source.put(name, value);
                }
                args[parameterWrapper.getIndex()] = value;
            }
        }
        return args;
    }

    @Override
    public Object[] beforeInvoke(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        try {
            args = processorLA(args, httpMethodWrapper);
            List<ParameterWrapper> wrapperList = httpMethodWrapper.getParameterByAnnotation(OpenItemTrigger.class);
            if (!CollectionUtils.isEmpty(wrapperList) && wrapperList.size() == 1){

                ParameterWrapper triggerWrapper = wrapperList.get(0);
                if (!ItemTrigger.class.isAssignableFrom(triggerWrapper.getType())){
                    return args;
                }
                args[triggerWrapper.getIndex()] = new ItemTrigger(collectParamterSource(httpMethodWrapper, args), itemResolver);
                return args;
            }
            return GlobalAroundResolver.super.beforeInvoke(args, httpMethodWrapper);
        }finally {
            clearMethod(httpMethodWrapper);
        }
    }

    protected Map<String, Object> collectParamterSource(HttpMethodWrapper httpMethodWrapper, Object[] args){
        Map<Method, Map<String, Object>> methodMap = sourceCache.get();
        if (methodMap == null){
            sourceCache.set(methodMap = new ConcurrentHashMap<>());
        }
        Method httpMethod = httpMethodWrapper.getHttpMethod();
        if (methodMap.containsKey(httpMethod)){
            return methodMap.get(httpMethod);
        }

        Map<String, Object> paramterSource = new HashMap<>();
        //collect params
        for (ParameterWrapper parameterWrapper : httpMethodWrapper.getParameterWrappers()) {

            //skip
            if (parameterWrapper.getAnnotationTypes().contains(OpenItemTrigger.class)) {
                continue;
            }
            Object arg = args[parameterWrapper.getIndex()];
            if (arg != null){
                if (Map.class.isAssignableFrom(parameterWrapper.getType())){
                    paramterSource.putAll((Map<? extends String, ?>) arg);
                    paramterSource.put(parameterWrapper.getName(), arg);
                }else {
                    Trust trust = AnnotationUtils.getAnnotation(parameterWrapper.getType(), Trust.class);
                    if (trust != null){
                        paramterSource.putAll(JsonUtils.toJson(arg));
                    }else {
                        paramterSource.put(parameterWrapper.getName(), arg);
                    }
                }
            }
        }
        methodMap.put(httpMethod, paramterSource);
        return paramterSource;
    }

    protected void clearMethod(HttpMethodWrapper httpMethodWrapper){
        Map<Method, Map<String, Object>> mapMap = sourceCache.get();
        Method httpMethod = httpMethodWrapper.getHttpMethod();
        if (mapMap != null){
            mapMap.remove(httpMethod);
        }
    }

    protected Map<String, Object> parseDefineVariables(String[] items){
        Map<String, Object> result = new HashMap<>();
        if (items != null && items.length != 1){
            for (String item : items) {
                if (StringUtils.hasText(item)){
                    String[] keyAndValue = item.split("=");
                    if (keyAndValue.length != 2){
                        throw new RuntimeException("la 方法定义变量格式错误: " + item);
                    }
                    result.put(keyAndValue[0], keyAndValue[1]);
                }
            }
        }
        return result;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("la", huy -> LAOperatorProcessor.class.isAssignableFrom(huy) &&
                BeanUtil.isSolidClass(huy) && AnnotationUtils.getAnnotation(huy, OperatorProcessor.class) != null);
        entry.instance(false);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("la".equals(resultBody.getAlias())){
            for (Object cs : resultBody.getCollectSource()) {
                if (itemResolver == null){
                    itemResolver = new ThreadLocalItemResolver(ReflexUtils::instance);
                }
                itemResolver.add((Class<? extends LAOperatorProcessor>) cs);
            }
        }
    }
}
