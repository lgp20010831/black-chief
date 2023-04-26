package com.black.javassist;

import com.black.function.Consumer;
import com.black.core.util.Av0;
import javassist.bytecode.annotation.MemberValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

public class CtAnnotation {

    private final Class<? extends java.lang.annotation.Annotation> javaAnnType;

    private final Map<String, ValueAndType> valueTypeMap = new LinkedHashMap<>();

    public CtAnnotation(Class<? extends Annotation> javaAnnType) {
        this.javaAnnType = javaAnnType;
    }

    public CtAnnotation addField(String name, Object value, Class<?> type){
        valueTypeMap.put(name, new ValueAndType(value, type));
        return this;
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
}
