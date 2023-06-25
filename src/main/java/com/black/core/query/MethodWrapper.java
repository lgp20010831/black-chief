package com.black.core.query;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.convert.ConvertUtils;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MethodWrapper implements Wrapper<Method>, ExecutableWrapper, ModifierInformationist{

    static final Map<Method, MethodWrapper> cache = new ConcurrentHashMap<>();

    public static MethodWrapper get(Method method){
        return cache.computeIfAbsent(method, MethodWrapper::new);
    }

    private final Method method;

    private ClassWrapper<?> classWrapper;

    private final Map<String, ParameterWrapper> parameterWrappers = new LinkedHashMap<>();


    private final ParameterWrapper[] parameterArray;

    private final Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();

    public MethodWrapper(@NonNull Method method) {
        this.method = method;
        Parameter[] parameters = method.getParameters();
        parameterArray = new ParameterWrapper[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ParameterWrapper parameterWrapper = new ParameterWrapper(parameter, i);
            parameterWrappers.put(parameter.getName(), parameterWrapper);
            parameterArray[i] = parameterWrapper;
        }
        for (Annotation annotation : method.getAnnotations()) {
            annotationMap.put(annotation.annotationType(), annotation);
        }
    }

    public boolean isNoParam(){
        return getParameterCount() == 0;
    }

    public boolean isSingleParam(){
        return getParameterCount() == 1;
    }

    public ParameterWrapper firstParam(){
        ParameterWrapper[] parameterArray = getParameterArray();
        if (parameterArray != null && parameterArray.length > 1){
            return parameterArray[0];
        }
        return null;
    }

    public ParameterWrapper indexParam(int index){
        ParameterWrapper[] parameterArray = getParameterArray();
        if (parameterArray != null){
            return parameterArray[index];
        }
        return null;
    }

    public boolean isVoid(){
        return void.class.equals(getReturnType());
    }

    @Override
    public int getModifiers(){
        return get().getModifiers();
    }

    public ParameterWrapper[] getParameterArray() {
        return parameterArray;
    }

    public List<ParameterWrapper> getArrayParams(){
        return Arrays.asList(parameterArray);
    }

    public ParameterWrapper getParam(int index){
        return parameterArray[index];
    }

    public Object invoke(Object obj, Object... args){
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            args = ConvertUtils.checkMethodArgs(args, getParameterTypes());
            return method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getParameterNames(){
        return parameterWrappers.keySet();
    }

    public Collection<ParameterWrapper> getParameterWrappersSet() {
        return parameterWrappers.values();
    }

    public Map<String, ParameterWrapper> getParameterWrappers() {
        return parameterWrappers;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type){
        return annotationMap.containsKey(type);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return (T) annotationMap.get(type);
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotationMap() {
        return annotationMap;
    }

    @SafeVarargs
    public final boolean parameterAnnotationExist(Class<? extends Annotation>... types){
        boolean exist = false;
        for (Class<? extends Annotation> type : types) {
            if (parameterHasAnnotation(type)) {
                exist = true;
            }
        }
        return exist;
    }

    public boolean parameterHasAnnotation(Class<? extends Annotation> type){
        for (ParameterWrapper wrapper : getParameterWrappersSet()) {
            if (wrapper.getAnnotationTypes().contains(type)) {
                return true;
            }
        }
        return false;
    }

    public ParameterWrapper getSingleParameterByType(@NonNull Class<?> type){
        List<ParameterWrapper> parameterByType = getParameterByType(type);
        return parameterByType.isEmpty() ? null : parameterByType.get(0);
    }

    public ParameterWrapper getParameter(String name){
        return parameterWrappers.get(name);
    }

    public List<ParameterWrapper> getParameterByType(@NonNull Class<?> type){
        List<ParameterWrapper> wrapperList = new ArrayList<>();
        for (ParameterWrapper parameterWrapper : getParameterWrappersSet()) {
            if (type.isAssignableFrom(parameterWrapper.getType())) {
                wrapperList.add(parameterWrapper);
            }
        }
        return wrapperList;
    }

    public ParameterWrapper getSingleParameterByAnnotation(Class<? extends Annotation> type){
        List<ParameterWrapper> parameterWrappers = getParameterByAnnotation(type);
        return parameterWrappers.isEmpty() ? null : parameterWrappers.get(0);
    }

    public List<ParameterWrapper> getParameterByAnnotation(Class<? extends Annotation> type){
        List<ParameterWrapper> wrapperList = new ArrayList<>();
        for (ParameterWrapper wrapper : getParameterWrappersSet()) {
            if (wrapper.getAnnotationTypes().contains(type)) {
                wrapperList.add(wrapper);
            }
        }
        return wrapperList;
    }

    public Class<?>[] getParameterTypes(){
        return method.getParameterTypes();
    }

    public int getParameterCount(){
        return method.getParameterCount();
    }

    public String getName(){
        return method.getName();
    }

    public Class<?> getReturnType(){
        return method.getReturnType();
    }

    public ClassWrapper<?> getDeclaringClassWrapper(){
        if (classWrapper == null){
            classWrapper = ClassWrapper.get(method.getDeclaringClass());
        }
        return classWrapper;
    }

    public Class<?> getDeclaringClass(){
        return method.getDeclaringClass();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof Method) return method.equals(o);
        if (o instanceof MethodWrapper){
            return ((MethodWrapper)o).method.equals(method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public Method get() {
        return method;
    }

    @Override
    public String toString() {
        return "@[" + hashCode() + "]wrapper -> " + get().toString();
    }
}
