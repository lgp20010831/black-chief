package com.black.core.tools;

import com.alibaba.fastjson.JSONObject;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.aop.servlet.HttpMethodManager;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.builder.Col;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.Ignore;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.util.Assert;
import com.black.core.util.ClassUtils;
import com.black.core.util.CurrentLineUtils;
import com.black.core.util.SetGetUtils;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class BeanUtil {

    private static final Collection<Class<?>> timeTypeList =
            Col.as(Timestamp.class, Date.class);

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    public static <T> T httpSet(Class<T> type){
        return httpSet(InstanceBeanManager.instance(type, InstanceType.REFLEX));
    }

    public static <T> T httpSet(T instance){
        HttpMethodWrapper hmw = HttpMethodManager.getHttpMethod();
        Assert.notNull(hmw, "current is not in http service");
        Object[] args = hmw.getArgs();
        MethodWrapper mw = hmw.getMethodWrapper();
        Map<String, Object> argMap = MapArgHandler.parse(args, mw);
        return mapping(instance, argMap);
    }


    public static <T> T autoSet(T instance, Object... args){
        Method method = CurrentLineUtils.loadMethod(1);
        MethodWrapper mw = MethodWrapper.get(method);
        Map<String, Object> argMap = MapArgHandler.parse(args, mw);
        return mapping(instance, argMap);
    }

    public static <T> T autoSet(Class<T> type, Object... args){
        T instance = InstanceBeanManager.instance(type, InstanceType.REFLEX);
        Method method = CurrentLineUtils.loadMethod(1);
        MethodWrapper mw = MethodWrapper.get(method);
        Map<String, Object> argMap = MapArgHandler.parse(args, mw);
        return mapping(instance, argMap);
    }

    public static boolean isSolidClass(Class<?> target){
        return !target.isInterface() && !target.isEnum() && !Modifier.isAbstract(target.getModifiers());
    }

    public static Map<String, Object> getVaildMap(Object bean){
        if (bean == null){
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        ClassWrapper<?> wrapper = ClassUtils.getClassWrapper(getPrimordialClass(bean));
        for (FieldWrapper field : wrapper.getFields()) {
            Object value = field.getValue(bean);
            if (value != null){
                map.put(field.getName(), value);
            }
        }
        return map;
    }



    public static <K, V> Map<K, V> repairMap(Map<K, V> map, Map<K, V> target){
        if (map == null || target == null){
            return map;
        }
        for (K k : map.keySet()) {
            V v = map.get(k);
            if (target.containsKey(k)){
                map.replace(k, target.get(k));
            }
        }
        return map;
    }

    public static <T> T mappingBean(Object dataBean, T targetBean){
        if (dataBean == null || targetBean == null) return targetBean;
        JSONObject json = JsonUtils.letJson(dataBean);
        return mapping(targetBean, json);
    }

    public static <T> T mapping(T bean, Map<String, Object> source){
        ClassWrapper<?> wrapper = ClassUtils.getClassWrapper(getPrimordialClass(bean));
        for (String fieldName : wrapper.getFieldNames()) {
            if (source.containsKey(fieldName)) {
                Object val = source.get(fieldName);
                SetGetUtils.invokeSetMethod(fieldName, val, bean);
            }
        }
        return bean;
    }

    public static <T> T toBean(Map<String, Object> map, Class<T> type){
        return BeanUtil.mapping(ReflexUtils.instance(type), map);
    }


    public static  <T, E> List<T> toBeanBatch(Collection<E> collection, Class<T> type){
        if (collection == null) return new ArrayList<>();
        ArrayList<T> list = new ArrayList<>();
        for (Object obj : collection) {
            JSONObject json = JsonUtils.letJson(obj);
            list.add(toBean(json, type));
        }
        return list;
    }

    public static <R> R attr(Object bean, String val, R defaultValue){
        if (bean == null){
            return defaultValue;
        }
        if (bean instanceof Map){
            Map<String, Object> map = (Map<String, Object>) bean;
            Object o = map.get(val);
            return o == null ? defaultValue : (R) o;
        }
        Object value = SetGetUtils.invokeGetMethod(val, bean);
        return value == null ? defaultValue : (R) value;
    }

    public static <T> T coryBean(T target, String... fieldNames){
        T instance = (T) ReflexUtils.instance(target.getClass());
        Class<?> targetClass = target.getClass();
        for (String fieldName : fieldNames) {
            Field field = ReflexUtils.getField(fieldName, targetClass);
            Object value = ReflexUtils.getValue(field, target);
            ReflexUtils.setValue(field, instance, value);
        }
        return instance;
    }
    public static ClassWrapper<?> getPrimordialClassWrapper(Object obj){
        return ClassWrapper.get(getPrimordialClass(obj));
    }

    public static <T> Class<T> getPrimordialClass(T obj){
        return getPrimordialClass(obj instanceof Class ? (Class<? extends T>) obj : (Class<? extends T>) obj.getClass());
    }

    public static <T> Class<T> getPrimordialClass(Class<? extends T> clazz){
        String name = clazz.getName();
        if (name.contains(CGLIB_CLASS_SEPARATOR)) {
            //isCglibProxy
            return getPrimordialClass((Class<? extends T>) clazz.getSuperclass());
        }else if (Proxy.isProxyClass(clazz)){
            //isJdkProxy
            return (Class<T>) clazz.getInterfaces()[0];
        }
        return (Class<T>) clazz;
    }

    public static <T> T fill(T target, Field field){
        DefaultValue value = AnnotationUtils.getAnnotation(field, DefaultValue.class);
        return value == null ? setNowTime(field, target) : wriedDefaultValueOfField(field, target);
    }

    public static Object getTimeValue(Class<?> type){
        if (type.equals(Date.class)){
            return new Date();
        }else if (type.equals(Timestamp.class)){
            return Timestamp.valueOf(
                    new SimpleDateFormat(DEFAULT_FORMAT).format(new Date()));
        }else if (type.equals(String.class)){
            return new SimpleDateFormat(DEFAULT_FORMAT).format(new Date());
        }
        return null;
    }

    public static <T> T wriedDefaultValue(T target){
        final Class<?> clazz = target.getClass();
        for (Field field : ReflexHandler.getAccessibleFields(clazz)){
            DefaultValue defaultValue = AnnotationUtils.getAnnotation(field, DefaultValue.class);
            if (defaultValue != null){
                try {
                    setDefaultValue(target, field, defaultValue.value());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return target;
    }

    public static <T> T setNowTime(String fieldName, T target){
        return setNowTime(ReflexUtils.getField(fieldName, target), target);
    }

    public static <T> T setNowTime(Field field, T target){
        try {
            setTime(target, field, DEFAULT_FORMAT);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    public static <T> T wriedDefaultValueOfField(String fieldName, T target){
        return wriedDefaultValueOfField(ReflexUtils.getField(fieldName, target), target);
    }

    public static <T> T wriedDefaultValueOfField(Field field, T target){
        DefaultValue value = AnnotationUtils.getAnnotation(field, DefaultValue.class);
        if (value != null){
            try {
                setDefaultValue(target, field, value.value());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return target;
    }

    /***
     * 自动填充指定对象中的时间类型的属性
     * @param target 目标对象
     * @return 返回填充后的对象
     */
    public static <T> T fillBean(T target)  {
        final Class<?> clazz = target.getClass();
        Time time;
        boolean needFillTime = (time = AnnotationUtils.getAnnotation(clazz, Time.class)) != null;
        String timeFormat = needFillTime ? time.value() : null;
        //检索字段
        for (Field field : ReflexHandler.getAccessibleFields(clazz)) {
            UUID uuid; DefaultValue defaultValue;
            final Class<?> type = field.getType();
            try{
                if (isNullValue(target, field)){
                    if (timeTypeList.contains(type)){
                        //证明字段是时间类型
                        if (needFillTime && AnnotationUtils.getAnnotation(field, IgnoreTime.class) == null){
                            setTime(target, field, timeFormat);
                            continue;
                        }
                    }
                    Time fieldTime = AnnotationUtils.getAnnotation(field, Time.class);
                    if (fieldTime != null){
                        setTime(target, field, fieldTime.value());
                        continue;
                    }
                    defaultValue = AnnotationUtils.getAnnotation(field, DefaultValue.class);
                    if (defaultValue != null){
                        setDefaultValue(target, field, defaultValue.value());
                        continue;
                    }
                    uuid = AnnotationUtils.getAnnotation(field, UUID.class);
                    if (uuid != null ){
                        field.set(target, java.util.UUID.randomUUID().toString());
                    }
                }
            }catch (Throwable e){
                throw new RuntimeException(e);
            }
        }
        return target;
    }

    public static void setTime(Object bean, Field field, String format) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (type.equals(Date.class)){
            field.set(bean, new Date());
        }else if (type.equals(Timestamp.class)){
            field.set(bean, Timestamp.valueOf(
                    new SimpleDateFormat(format).format(new Date())));
        }else if (type.equals(String.class)){
            field.set(bean, new SimpleDateFormat(format).format(new Date()));
        }
    }

    public static void setDefaultValue(Object bean, Field field, String value) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (type.equals(String.class)){
            field.set(bean, value);
        }else if (Boolean.class.isAssignableFrom(type)){
            field.set(bean, Boolean.valueOf(value));
        }else if (type.equals(Double.class)){
            field.set(bean, Double.valueOf(value));
        }else if (type.equals(Integer.class)){
            field.set(bean, Integer.parseInt(value));
        }
    }

    public static Object getDefaultValue(Class<?> type, String value){
        if (type.equals(String.class)){
            return value;
        }else if (Boolean.class.isAssignableFrom(type)){
            return Boolean.valueOf(value);
        }else if (type.equals(Double.class)){
            return Double.valueOf(value);
        }else if (type.equals(Integer.class)){
            return Integer.parseInt(value);
        }else {
            TypeHandler typeHandler = TypeConvertCache.initAndGet();
            if (typeHandler != null){
                return typeHandler.convert(type, value);
            }
        }
        return value;
    }

    public static Object getDefaultValue2(Class<?> type, String value){
        if (type.equals(String.class)){
            return value;
        }else if (Boolean.class.isAssignableFrom(type)){
            return Boolean.valueOf(value);
        }else if (type.equals(Double.class)){
            return Double.valueOf(value);
        }else if (type.equals(Integer.class)){
            return Integer.parseInt(value);
        }else {
            TypeHandler typeHandler = TypeConvertCache.initAndGet();
            if (typeHandler != null){
                return typeHandler.convert(type, value);
            }
        }
        return value;
    }


    public static boolean isNullValue(Object bean, Field field){
        try {
            return field.get(bean) == null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNullBean(Object bean){
        for (Field field : ReflexHandler.getAccessibleFields(bean)) {
            Object o;
            try {
                o = field.get(bean);
            } catch (IllegalAccessException e) {
                return false;
            }
            if (AnnotationUtils.getAnnotation(field, Ignore.class) != null)
                continue;
            if (o != null)
                return false;
        }
        return true;
    }

    /** string 转向 uuid */
    public static java.util.UUID strToId(String id)  {return java.util.UUID.fromString(id);}

    /** uuid to string */
    public static String idToUUID(UUID id) {return id.toString();}

    /** str to timestamp */
    public static Timestamp strToTimestamp(String str)  {return Timestamp.valueOf(str);}

    /** timestamp to str */
    public static String timestampToStr(Timestamp timestamp)    {return timestamp.toString();}

    /** create timestamp */
    public static Timestamp currentTimestamp()  {return new Timestamp(new Date().getTime());}

    public static Timestamp dateToTimestamp(Date date)  {return new Timestamp(date.getTime());}

    public static Date timestampToDate(Timestamp timestamp) {
        return new Date(timestamp.getTime());}

    public static String toJsonStr(Object bean){
        return toJson(bean).toString();
    }

    public static JSONObject toJson(Object bean){
        return (JSONObject) JSONObject.toJSON(bean);
    }
}
