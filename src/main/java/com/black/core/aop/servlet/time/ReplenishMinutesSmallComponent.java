package com.black.core.aop.servlet.time;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;
import com.black.core.util.SetGetUtils;
import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;

import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@GlobalAround @SuppressWarnings("all")
public class ReplenishMinutesSmallComponent implements GlobalAroundResolver {

    private final Map<Parameter, RepairTimeConfig> configCache = new ConcurrentHashMap<>();

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper mw) {
        MethodWrapper methodWrapper = mw.getMethodWrapper();
        List<ParameterWrapper> pws = methodWrapper.getParameterByAnnotation(RepairTime.class);
        if (!Utils.isEmpty(pws)){
            for (ParameterWrapper pw : pws) {
                processRepairTime(pw, args, mw);
            }
        }
        List<ParameterWrapper> timeCompletionPws = methodWrapper.getParameterByAnnotation(TimeCompletion.class);
        if (!Utils.isEmpty(timeCompletionPws)){
            for (ParameterWrapper timeCompletionPw : timeCompletionPws) {
                processTimeCompletion(timeCompletionPw, args, mw);
            }
        }
        return GlobalAroundResolver.super.handlerArgs(args, mw);
    }

    public void processTimeCompletion(ParameterWrapper pw, Object[] args, HttpMethodWrapper mw){
        TimeCompletion annotation = pw.getAnnotation(TimeCompletion.class);
        String[] value = annotation.value();

        //要修复的参数
        Object target = args[pw.getIndex()];
        if (target == null){
            return;
        }
        Set<String> startTimeKeyNames = new HashSet<>();
        Set<String> endTimeKeyNames = new HashSet<>();
        for (String s : value) {
            String[] kv = s.trim().split("->");
            if (kv.length > 1){
                startTimeKeyNames.add(kv[0]);
            }
            if (kv.length > 2){
                endTimeKeyNames.add(kv[1]);
            }
        }
        RepairTimeConfig config = new RepairTimeConfig();
        config.setAppendEndTime(annotation.appendEndTime());
        config.setAppendStartTime(annotation.appendStartTime());
        for (String startTimeKeyName : startTimeKeyNames) {
            Object val = ServiceUtils.getProperty(target, startTimeKeyName);
            Object after = doProcessorStartTime(pw, args, mw, val, config);
            ServiceUtils.setProperty(target, startTimeKeyName, after);
        }

        for (String endTimeKey : endTimeKeyNames) {
            Object val = ServiceUtils.getProperty(target, endTimeKey);
            Object after = doProcessorEndTime(pw, args, mw, val, config);
            ServiceUtils.setProperty(target, endTimeKey, after);
        }
    }

    public void processRepairTime(ParameterWrapper pw, Object[] args, HttpMethodWrapper mw){
        RepairTime annotation = pw.getAnnotation(RepairTime.class);
        Assert.notNull(annotation, "@RepairTime is null");
        RepairTimeConfig config = configCache.computeIfAbsent(pw.get(), raw -> {
            return AnnotationUtils.loadAttribute(annotation, new RepairTimeConfig());
        });
        String startTimeName = config.getStartTimeName();
        String endTimeName = config.getEndTimeName();
        //要修复的参数
        Object target = args[pw.getIndex()];
        if (target == null){
            return;
        }
        String name = pw.getName();
        if (target instanceof Map){
            Map<String, Object> map = (Map<String, Object>) target;
            if (map.containsKey(startTimeName)){
                Object afterValue = doProcessorStartTime(pw, args, mw, map.get(startTimeName), config);
                map.put(startTimeName, afterValue);
            }

            if (map.containsKey(endTimeName)){
                Object afterValue = doProcessorEndTime(pw, args, mw, map.get(endTimeName), config);
                map.put(endTimeName, afterValue);
            }
        }else if (startTimeName.equals(name)){
            Object afterValue = doProcessorStartTime(pw, args, mw, target, config);
            args[pw.getIndex()] = afterValue;
        }else if (endTimeName.equals(name)){
            Object afterValue = doProcessorEndTime(pw, args, mw, target, config);
            args[pw.getIndex()] = afterValue;
        }else{
            Class<Object> targetType;
            ClassWrapper<?> classWrapper = ClassWrapper.get(targetType = BeanUtil.getPrimordialClass(target));
            if (SetGetUtils.hasGetMethod(targetType, startTimeName)){
                Object afterValue = doProcessorStartTime(pw, args, mw, SetGetUtils.invokeGetMethod(startTimeName, target), config);
                SetGetUtils.invokeSetMethod(startTimeName, afterValue, target);
            }

            if (SetGetUtils.hasGetMethod(targetType, endTimeName)){
                Object afterValue = doProcessorEndTime(pw, args, mw, SetGetUtils.invokeGetMethod(endTimeName, target), config);
                SetGetUtils.invokeSetMethod(endTimeName, afterValue, target);
            }
        }
    }

    public Object doProcessorEndTime(ParameterWrapper pw, Object[] args, HttpMethodWrapper mw, Object arg, RepairTimeConfig config){
        if (arg == null){
            return arg;
        }
        String argStr = arg.toString();
        String result = null;
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        Class<?> type = BeanUtil.getPrimordialClass(arg);
        Class<? extends RepairTimePlug> plugClass = config.getPlugClass();
        if (!plugClass.equals(NoOperationPlug.class)){
            RepairTimePlug plug = beanFactory.getSingleBean(plugClass);
            try {
                result = plug.repairEndTime(pw, argStr, args, mw);
            } catch (Throwable e) {
                CentralizedExceptionHandling.handlerException(e);
                return arg;
            }
        }else {
            result = argStr + config.getAppendEndTime();
        }
        Object finalResult = result;
        if (result != null){
            if (!String.class.equals(type)){
                TypeHandler typeHandler = TypeConvertCache.initAndGet();
                finalResult = typeHandler.convert(type, result);
            }
        }
        return finalResult;
    }

    public Object doProcessorStartTime(ParameterWrapper pw, Object[] args, HttpMethodWrapper mw, Object arg, RepairTimeConfig config){
        if (arg == null){
            return arg;
        }
        String argStr = arg.toString();
        String result = null;
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        Class<?> type = BeanUtil.getPrimordialClass(arg);
        Class<? extends RepairTimePlug> plugClass = config.getPlugClass();
        if (!plugClass.equals(NoOperationPlug.class)){
            RepairTimePlug plug = beanFactory.getSingleBean(plugClass);
            try {
                result = plug.repairStartTime(pw, argStr, args, mw);
            } catch (Throwable e) {
                CentralizedExceptionHandling.handlerException(e);
                return arg;
            }
        }else {
            result = argStr + config.getAppendStartTime();
        }
        Object finalResult = result;
        if (result != null){
            if (!String.class.equals(type)){
                TypeHandler typeHandler = TypeConvertCache.initAndGet();
                finalResult = typeHandler.convert(type, result);
            }
        }
        return finalResult;
    }
}
