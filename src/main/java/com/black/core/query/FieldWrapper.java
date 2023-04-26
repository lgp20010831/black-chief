package com.black.core.query;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FieldWrapper implements Wrapper<Field>, GenericWrapper{

    static final Map<Field, FieldWrapper> cache = new ConcurrentHashMap<>();

    public static FieldWrapper get(Field field){
        return cache.computeIfAbsent(field, FieldWrapper::new);
    }

    private final Field field;

    private Boolean isGeneric = null;

    private ClassWrapper<?> declaringClass;

    private final Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();

    public FieldWrapper(Field field) {
        this.field = field;
        for (Annotation annotation : field.getAnnotations()) {
            annotationMap.put(annotation.annotationType(), annotation);
        }

    }

    public boolean isGenericType(){
        if (isGeneric == null){
            String desc = getField().getGenericType().getTypeName();
            try {
                Class.forName(desc);
                isGeneric = true;
            } catch (ClassNotFoundException e) {
                isGeneric = false;
            }
        }
        return isGeneric;
    }

    public boolean isNull(Object bean){
        return getValue(bean) == null;
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }

    public Object getValue(Object obj){
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassWrapper<?> getDeclaringClass(){
        if (declaringClass == null){
            declaringClass = ClassWrapper.get(field.getDeclaringClass());
        }
        return declaringClass;
    }

    public <T> T setNullValue(T obj){
        return setValue(obj, null);
    }

    public <T> T setValue(T obj, Object value){
        try {
            if (value != null){
                if (!getType().isAssignableFrom(value.getClass())) {
                    TypeHandler handler = TypeConvertCache.initAndGet();
                    if (handler != null){
                        value = handler.convert(getType(), value);
                    }
                }
            }
            if (!field.isAccessible()){
                field.setAccessible(true);
            }
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())){
                try {
                    setFinalStatic(get(), value);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                return obj;
            }
            field.set(obj, value);
            return obj;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    public Class<?> getType(){
        return field.getType();
    }

    public <T> T getValue(Object obj, Class<T> type){
        try {
            Object value = field.get(obj);
            if (value == null){
                return (T) value;
            }

            if (!type.isAssignableFrom(value.getClass())){
                TypeHandler handler = TypeConvertCache.initAndGet();
                if (handler != null){
                    value = handler.convert(type, value);
                }
            }
            return (T) value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Field getField() {
        return field;
    }

    public String getName(){
        return field.getName();
    }

    public Set<Class<? extends Annotation>> getAnnotationTypes(){
        return annotationMap.keySet();
    }

    public boolean hasAnnotation(Class<? extends Annotation> type){
        return annotationMap.containsKey(type);
    }

    public Collection<Annotation> getAnnotations(){
        return annotationMap.values();
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotationMap() {
        return annotationMap;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
        return (T) annotationMap.get(annotationClass);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof Field) return field.equals(o);
        if (o instanceof FieldWrapper){
            return ((FieldWrapper)o).field.equals(field);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public Field get() {
        return field;
    }

    @Override
    public String toString() {
        return "@[" + hashCode() + "]wrapper -> " + get().toString();
    }
}
