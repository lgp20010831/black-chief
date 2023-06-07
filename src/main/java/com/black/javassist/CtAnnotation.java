package com.black.javassist;

import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;
import com.black.function.Consumer;
import com.black.core.util.Av0;
import javassist.bytecode.annotation.MemberValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@SuppressWarnings("all")
public class CtAnnotation {

    private final Class<? extends java.lang.annotation.Annotation> javaAnnType;

    private final Map<String, ValueAndType> valueTypeMap = new LinkedHashMap<>();

    public CtAnnotation(Class<? extends Annotation> javaAnnType) {
        this.javaAnnType = javaAnnType;
    }

    public CtAnnotation(@NonNull Annotation annotation){
        this.javaAnnType = annotation.annotationType();
        Map<String, Object> valueMap = AnnotationUtils.getAnnotationValueMap(annotation, false);
        valueMap.forEach((k, v) -> {
            addField(k, v);
        });
    }

    public CtAnnotation addField(String name, Object value){
        AnnotationTypeWrapper typeWrapper = AnnotationTypeWrapper.get(javaAnnType);
        MethodWrapper methodWrapper = typeWrapper.select(name);
        Assert.notNull(methodWrapper, "can not find method: " + name + " on annotation: @" + javaAnnType.getSimpleName());
        return addField(name, value, methodWrapper.getReturnType());
    }

    public CtAnnotation addField(String name, Object value, Class<?> type){
        valueTypeMap.put(name, new ValueAndType(value, type));
        return this;
    }

    public Class<? extends Annotation> getJavaAnnType() {
        return javaAnnType;
    }

    @AllArgsConstructor
    @Getter
    static class ValueAndType{
        private final Object value;
        private final Class<?> type;
    }

    public Map<Class<? extends java.lang.annotation.Annotation>,
            Consumer<javassist.bytecode.annotation.Annotation>> getAnnotationCallback(){
        return Av0.of(javaAnnType, annotation -> {
            for (String name : valueTypeMap.keySet()) {
                ValueAndType valueAndType = valueTypeMap.get(name);
                Class<?> type = valueAndType.getType();
                Object value = valueAndType.getValue();
                MemberValue memberValue = Utils.getMemberValue(type, value);
                annotation.addMemberValue(name, memberValue);
            }
        });
    }

    @Override
    public String toString() {
        String annName = "@" + javaAnnType.getSimpleName();
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        valueTypeMap.forEach((n, v) -> {
            Object value = v.value;
            if (value == null){
                value = "";
            }else {
                if (value.getClass().isArray()) {
                    value = Arrays.toString((Object[]) value);
                }
            }
            joiner.add(n + "=" + value);
        });
        return annName + joiner;
    }
}
