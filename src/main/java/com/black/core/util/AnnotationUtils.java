package com.black.core.util;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.Alias;
import com.black.core.query.*;
import com.black.core.tools.BeanUtil;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationUtils {

    public static <T extends Annotation> T obtainAnnotation(Method method, Class<T> type){
        return obtainAnnotation(method.getDeclaringClass(), method, type);
    }

    public static <T extends Annotation> T obtainAnnotation(Class<?> clazz, Method method, Class<T> type){
        T annotation = findAnnotation(method, type);
        if (annotation == null){
            annotation = findAnnotation(clazz, type);
        }
        return annotation;
    }

    public static Object getValueFromAnnotation(Annotation annotation){
        return getValueFromAnnotation(annotation, "value");
    }

    /** 拿取注解上的值 */
    public static Object getValueFromAnnotation(Annotation annotation, String key) {

        if (annotation == null)
            return null;

        try {

            return annotation.getClass().getMethod(key).invoke(annotation);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("get value from annotation", e);
        }
    }


    /*
        将指定对象转换成 json 类型, 只会读取指定注解标注的字段
        关于 key 会读取该注解名为 name 的方法, 也可以用 alias 注解
        标注在注解方法上实现修改方法名称
     */
    public static JSONObject readJsonOfAnnotation(@NonNull Object target, @NonNull Class<? extends Annotation> annotationType){
        ClassWrapper<?> cw = BeanUtil.getPrimordialClassWrapper(target);
        JSONObject json = new JSONObject();
        Collection<FieldWrapper> fields = cw.getFields();
        AnnotationTypeWrapper atw = AnnotationTypeWrapper.get(annotationType);
        String targetMethodName = null;
        for (MethodWrapper mw : atw.getAnnotationMethodCollection()) {
            if("name".equals(mw.getName())){
                targetMethodName = mw.getName();
                break;
            }

            Alias annotation = mw.getAnnotation(Alias.class);
            if (annotation != null && "name".equals(annotation.value())){
                targetMethodName = mw.getName();
                break;
            }
        }
        if (targetMethodName == null){
            throw new IllegalStateException("current annotation is not exist name method");
        }
        for (FieldWrapper field : fields) {
            Annotation annotation = field.getAnnotation(annotationType);
            if (annotation != null){
                String key = String.valueOf(atw.getValue(targetMethodName, annotation));
                if (!StringUtils.hasText(key) || "null".equals(key)){
                    key = field.getName();
                }
                Object value = field.getValue(target);
                json.put(key, value);
            }
        }
        return json;
    }

    private static final Map<Class<? extends Annotation>, AnnotationTypeWrapper> typeWrapperCache = new ConcurrentHashMap<>();

    public static <T> T loadAttribute(Annotation annotation, T bean){
        return loadAttribute(annotation, bean, true);
    }

    public static <T> T loadAttribute(Annotation annotation, T bean, boolean tile){
        Class<? extends Annotation> type = annotation.annotationType();
        AnnotationTypeWrapper typeWrapper = typeWrapperCache.computeIfAbsent(type, AnnotationTypeWrapper::new);

        //拿到该注解上的所有注解
        Collection<Annotation> annotations = typeWrapper.getAnnotations();
        ClassWrapper<?> beanWrapper = ClassUtils.getClassWrapper(BeanUtil.getPrimordialClass(bean));
        
        //拿到原注解的属性
        Map<String, Object> annotationValueMap = getAnnotationValueMap(annotation, tile);
        for (Annotation parasitic : annotations) {
            Map<String, Object> parasiticValueMap = getAnnotationByAnnotation(annotation, parasitic.annotationType());
            annotationValueMap.putAll(parasiticValueMap);
        }
        return BeanUtil.mapping(bean, annotationValueMap);
    }

    public static Map<String, Object> getAnnotation(AnnotatedElement element, Class<? extends Annotation> target){
        Annotation annotation = element.getAnnotation(target);
        if(annotation != null){
            return null;
        }

        Annotation[] annotations = element.getAnnotations();
        for (Annotation at : annotations) {
            Class<? extends Annotation> type = at.annotationType();
            if (type.getName().startsWith("java.lang.annotation")) {
                continue;
            }
            if (type.isAnnotationPresent(target)) {
                return getAnnotationByAnnotation(at, target);
            }
        }
        return null;
    }

    public static Map<String, Object> getAnnotationByAnnotation(@NonNull Annotation annotation,
                                                                @NonNull Class<? extends Annotation> target){
        return getAnnotationByAnnotation(annotation, target, true);
    }

    //获取该注解上, 指定类型注解的所有值
    public static Map<String, Object> getAnnotationByAnnotation(@NonNull Annotation annotation,
                                                                @NonNull Class<? extends Annotation> target,
                                                                boolean tile){
        Class<? extends Annotation> type = annotation.annotationType();
        AnnotationTypeWrapper typeWrapper = typeWrapperCache.computeIfAbsent(type, AnnotationTypeWrapper::new);
        AnnotationTypeWrapper targetWrapper = typeWrapperCache.computeIfAbsent(target, AnnotationTypeWrapper::new);
        if (!typeWrapper.hasAnnotation(target)) {
            return new HashMap<>();
        }

        Annotation targetAnnotation = typeWrapper.getAnnotation(target);
        Map<String, Object> valueMap = getAnnotationValueMap(targetAnnotation, tile);
        boolean only = typeWrapper.annotationSize() == 1;

        for (MethodWrapper methodWrapper : typeWrapper.getAnnotationMethodCollection()) {
            if (methodWrapper.hasAnnotation(AliasWith.class)) {
                AliasWith aliasWith = methodWrapper.getAnnotation(AliasWith.class);
                Class<? extends Annotation> point = aliasWith.target();
                String name = StringUtils.hasText(aliasWith.name()) ? aliasWith.name() : methodWrapper.getName();
                if (point.equals(target) || (point.equals(Annotation.class) && only)){
                    //对应上 target
                    if (targetWrapper.contain(name)) {

                        //类型相同
                        if (targetWrapper.getType(name).equals(methodWrapper.getReturnType())){
                            valueMap.put(name, methodWrapper.invoke(annotation));
                        }
                    }
                }
            }
        }

        return valueMap;
    }


    public static Map<String, Object> getAnnotationValueMap(Annotation annotation, boolean attributeTile){
        if (annotation == null){
            return new HashMap<>();
        }
        Class<? extends Annotation> type = annotation.annotationType();
        AnnotationTypeWrapper typeWrapper = typeWrapperCache.computeIfAbsent(type, AnnotationTypeWrapper::new);
        Map<String, Object> map = new HashMap<>();

        for (MethodWrapper methodWrapper : typeWrapper.filterByContainAnnotation(AliasMapping.class)) {
            Object value = methodWrapper.invoke(annotation);
            AliasMapping aliasMapping = methodWrapper.getAnnotation(AliasMapping.class);
            String attribute = aliasMapping.attribute();
            String aname = StringUtils.hasText(aliasMapping.name()) ? aliasMapping.name() : methodWrapper.getName();
            MethodWrapper wrapper = typeWrapper.select(attribute);
            if (wrapper != null){
                Object attirbuteAnntation = wrapper.invoke(annotation);
                Map<String, Object> valueMap = getAnnotationValueMap((Annotation) attirbuteAnntation, attributeTile);
                valueMap.put(aname, value);
                if (attributeTile) {map.putAll(valueMap); map.put(attribute, true);}
                else map.put(attribute, valueMap);
            }
        }

        typeWrapper.getAnnotationMethods().forEach((name, method) ->{
            String alias = method.hasAnnotation(Alias.class) ? method.getAnnotation(Alias.class).value() : name;
            if (map.containsKey(alias)){
                return;
            }

            Object value = method.invoke(annotation);
            //如果方法类型是一个注解
            if (Annotation.class.isAssignableFrom(method.getReturnType())){
                value = getAnnotationValueMap((Annotation) value, attributeTile);
            }
            if ((value instanceof Map) && attributeTile)
                map.putAll((Map<? extends String, ?>) value);
            else map.put(alias, value);
        });
        return map;
    }

    public static <T extends Annotation> T findAnnotation(Wrapper<?> wrapper, @NonNull Class<T> type){
        return findAnnotation((AnnotatedElement) wrapper.get(), type);
    }

    public static <T extends Annotation> T findAnnotation(AnnotatedElement element, @NonNull Class<T> type){
        if (element == null){
            return null;
        }
        try {
            Class.forName("org.springframework.core.annotation.AnnotatedElementUtils");
            return AnnotatedElementUtils.findMergedAnnotation(element, type);
        } catch (ClassNotFoundException e) {
            return element.getAnnotation(type);
        }
    }

    public static <T extends Annotation> boolean isPertain(AnnotatedElement element, @NonNull Class<T> type){
        if (element == null){
            return false;
        }
        try {
            Class.forName("org.springframework.core.annotation.AnnotatedElementUtils");
            return org.springframework.core.annotation.AnnotationUtils.getAnnotation(element, type) != null;
        } catch (ClassNotFoundException e) {
            return element.isAnnotationPresent(type);
        }
    }

}
