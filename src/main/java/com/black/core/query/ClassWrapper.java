package com.black.core.query;

import com.black.core.chain.GroupKeys;
import com.black.core.convert.ConvertUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Av0;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.utils.ReflexHandler;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ClassWrapper<T> implements Wrapper<Class<T>>, GenericWrapper{

    private static final Map<Class<?>, ClassWrapper<?>> cache = new ConcurrentHashMap<>();

    public  static <B> ClassWrapper<B> get(Class<B> cl){
        return (ClassWrapper<B>) cache.computeIfAbsent(cl, ClassWrapper::new);
    }

    private final Class<T> primordialClass;

    private static final char PACKAGE_SEPARATOR = '.';

    public static Set<String> basicTypeName;

    public static Set<String> basicWrapperTypeName;

    static {
        basicTypeName = Av0.set("int", "double", "float", "long", "byte", "short", "boolean", "char");
        basicWrapperTypeName = Av0.set("java.lang.Integer", "java.lang.Double", "java.lang.Float", "java.lang.Long", "java.lang.Byte",
                "java.lang.Short", "java.lang.Boolean", "java.lang.Character");
    }

    public static void clearCache(){
        StringJoiner joiner = new StringJoiner("\n");
        synchronized (cache){
            joiner.add("class before clear: " + cache.size() + ", after clear: 0");
            cache.clear();
        }

        synchronized (ConstructorWrapper.cache) {
            joiner.add("constructor before clear: " + ConstructorWrapper.cache.size() + ", after clear: 0");
            ConstructorWrapper.cache.clear();
        }

        synchronized (FieldWrapper.cache){
            joiner.add("field before clear: " + FieldWrapper.cache.size() + ", after clear: 0");
            FieldWrapper.cache.clear();
        }


        synchronized (MethodWrapper.cache){
            joiner.add("method before clear: " + MethodWrapper.cache.size() + ", after clear: 0");
            MethodWrapper.cache.clear();
        }
        System.out.println(joiner);
    }

    public static Class<?> pack(String type){
        switch (type){
            case "int":
                return Integer.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "long":
                return Long.class;
            case "byte":
                return Byte.class;
            case "short":
                return Short.class;
            case "boolean":
                return Boolean.class;
            case "char":
                return Character.class;
            default:
                throw new IllegalArgumentException("is not a basic type");
        }
    }
    public static String getUnpack(String name){
        if (isBasic(name)){
            return name;
        }
        switch (name){
            case "Integer":
                return "int";
            case "Double":
                return "double";
            case "Float":
                return "float";
            case "Long":
                return "long";
            case "Byte":
                return "byte";
            case "Short":
                return "short";
            case "Boolean":
                return "boolean";
            case "Character":
                return "char";
            default:
                throw new IllegalArgumentException("is not a basic pack type");
        }
    }

    public static Class<?> unpacking(String type){
        switch (type){
            case "Integer":
                return int.class;
            case "Double":
                return double.class;
            case "Float":
                return float.class;
            case "Long":
                return long.class;
            case "Byte":
                return byte.class;
            case "Short":
                return short.class;
            case "Boolean":
                return boolean.class;
            case "Character":
                return char.class;
            default:
                throw new IllegalArgumentException("is not a basic pack type");
        }
    }

    public JHexByteArrayInputStream read(){
        Class<T> tClass = get();
        return null;
    }

    public static boolean autoAssemblyAndDisassembly(Class<?> src, Class<?> target){
        String srcName = src.getName();
        String targetName = target.getName();
        if (srcName.equals(targetName)){
            return true;
        }
        String srcSimpleName = src.getSimpleName();
        if (isBasicWrapper(srcName) && isBasic(targetName)){
            return targetName.equals(unpacking(src.getSimpleName()).getName());
        }else if (isBasicWrapper(targetName) && isBasic(srcName)){
            return srcName.equals(unpacking(target.getSimpleName()).getName());
        }
        return false;
    }

    public static boolean isBasicWrapper(String name){
        return basicWrapperTypeName.contains(name);
    }

    public static boolean isBasic(String name){
        return basicTypeName.contains(name);
    }

    private final Map<String, List<MethodWrapper>> methodCache = new HashMap<>();

    private final Map<String, FieldWrapper> fieldCache = new HashMap<>();

    private final Map<GroupKeys, ConstructorWrapper<?>> constructorWrappers = new HashMap<>();

    private final Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();

    public ClassWrapper(@NonNull Class<T> clazz) {
        this.primordialClass = clazz;
        if (!clazz.isInterface()){
            for (Field field : ReflexHandler.getAccessibleFields(clazz, true)) {
                fieldCache.put(field.getName(), FieldWrapper.get(field));
            }

            for (Constructor<?> constructor : clazz.getConstructors()) {
                constructorWrappers.put(new GroupKeys((Object[]) constructor.getParameterTypes()), ConstructorWrapper.get(constructor));
            }
        }
        for (Method method : ReflexUtils.getMethods(clazz)) {
            if (!Object.class.equals(method.getDeclaringClass())){
                List<MethodWrapper> methodWrappers = methodCache.computeIfAbsent(method.getName(), name -> new ArrayList<>());
                methodWrappers.add(MethodWrapper.get(method));
            }
        }

        for (Annotation annotation : primordialClass.getAnnotations()) {
            annotationMap.put(annotation.annotationType(), annotation);
        }
    }

    public T instance(Object... args){
        return securityInstance(false, args);
    }

    public T securityInstance(boolean check, Object... args){
        return securityInstance(check, null, args);
    }

    public T securityInstance(boolean check, Class<?>[] types, Object... args){
        if (args.length == 0){
            try {
                return getPrimordialClass().newInstance();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        ConstructorWrapper<?> cw = null;
        if (types != null){
            cw = getConstructor(types);
        }else {
            Collection<ConstructorWrapper<?>> constructors = getConstructorWrappers();
            for (ConstructorWrapper<?> constructor : constructors) {
                if (constructor.getParamCount() == args.length){
                    cw = constructor;
                    break;
                }
            }
        }
        if (cw == null){
            throw new IllegalStateException("can not find constructor in the description:" + Arrays.toString(types));
        }
        if (check){
            args = ConvertUtils.checkMethodArgs(args, cw.getParameterTypes());
        }
        return (T) cw.newInstance(args);
    }

    @Override
    public Type getGenericType() {
        return primordialClass.getGenericSuperclass();
    }

    public Class<T> getPrimordialClass() {
        return primordialClass;
    }

    public String getName(){
        return primordialClass.getName();
    }

    public FieldWrapper getField(String name){
        return fieldCache.get(name);
    }

    public Class<?> getFieldType(String name){
        if (fieldCache.containsKey(name)){
            return fieldCache.get(name).getType();
        }
        throw new RuntimeException("没有该字段 " + name);
    }

    public Map<String, FieldWrapper> getFieldCache() {
        return fieldCache;
    }

    public Map<String, List<MethodWrapper>> getMethodCache() {
        return methodCache;
    }

    public Collection<FieldWrapper> getFields(){
        return fieldCache.values();
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotationMap() {
        return annotationMap;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type){
        return getAnnotationMap().containsKey(type);
    }

    public boolean inlayAnnotation(Class<? extends Annotation> type){
        return AnnotationUtils.getAnnotation(primordialClass, type) != null;
    }

    public <T extends Annotation> T getMergeAnnotation(Class<T> type){
        return AnnotatedElementUtils.findMergedAnnotation(primordialClass, type);
    }

    public <T> T getAnnotation(Class<T> type){
        return (T) getAnnotationMap().get(type);
    }

    public Set<String> getFieldNames(){
        return fieldCache.keySet();
    }

    public Set<String> getMethodNames(){
        return methodCache.keySet();
    }

    public MethodWrapper queryMethod(Method method){
        for (MethodWrapper methodWrapper : getMethods()) {
            if (methodWrapper.getMethod().equals(method)){
                return methodWrapper;
            }
        }
        return null;
    }

    public Collection<MethodWrapper> getMethods(){
        Collection<List<MethodWrapper>> lists = methodCache.values();
        Collection<MethodWrapper> wrappers = new HashSet<>();
        for (List<MethodWrapper> list : lists) {
            wrappers.addAll(list);
        }
        return wrappers;
    }

    public String getSimpleName(){
        return primordialClass.getSimpleName();
    }

    public boolean isSoild(){
        return BeanUtil.isSolidClass(primordialClass);
    }

    public String getPackageName(){
        int lastDotIndex = getName().lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? getName().substring(0, lastDotIndex) : "");
    }

    public Class<?> getSuperClass(){
        return primordialClass.getSuperclass();
    }


    public static <I> Collection<Class<?>> getLoopInterfaces(Class<I> targetInterface){
        if (targetInterface == null || !targetInterface.isInterface()){
            throw new RuntimeException("参数必须不能为空,且是一个接口:" + targetInterface);
        }
        Collection<Class<?>> result = new ArrayList<>();
        Class<?>[] interfaces = targetInterface.getInterfaces();
        if (interfaces.length != 0){
            for (Class<?> anInterface : interfaces) {
                result.add(anInterface);
                result.addAll(getLoopInterfaces(anInterface));
            }
        }
        return result;
    }

    public Set<Class<?>> getAssignableFromClasses(){
        return getAssignableFromClasses(primordialClass);
    }

    public static Set<Class<?>> getAssignableFromClasses(Class<?> target){
        Set<Class<?>> superClasses = getSuperClasses(target);
        superClasses.addAll(getInterfaces(target));
        return superClasses;
    }

    public static Set<Class<?>> getSuperClasses(Class<?> target){
        Set<Class<?>> result = new LinkedHashSet<>();
        Class<?> current = target.getSuperclass();
        while (current != null){
            result.add(current);
            current = current.getSuperclass();
        }
        return result;
    }

    public static Set<Class<?>> getInterfaces(Class<?> target){
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = target;
        while (current != null) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                interfaces.addAll(getLoopInterfaces(ifc));
            }
            interfaces.addAll(Arrays.asList(ifcs));
            current = current.getSuperclass();
        }
        return interfaces;
    }

    public Set<Class<?>> getInterfaces(){
        return getInterfaces(primordialClass);
    }

    public MethodWrapper getSingleMethod(String name){
        List<MethodWrapper> methods = getExpectMethod(name);
        return methods == null || methods.isEmpty() ? null : methods.get(0);
    }

    public List<MethodWrapper> getExpectMethod(String name){
        return methodCache.get(name);
    }

    public FieldWrapper getSingleFieldByAnnotation(Class<? extends Annotation> type){
        List<FieldWrapper> result = getFieldByAnnotation(type);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<FieldWrapper> getFieldByAnnotation(Class<? extends Annotation> type){
        List<FieldWrapper> result = new ArrayList<>();
        for (FieldWrapper wrapper : fieldCache.values()) {
            if (wrapper.hasAnnotation(type)) {
                result.add(wrapper);
            }
        }
        return result;
    }

    public FieldWrapper getSingleFieldByType(Class<?> type){
        List<FieldWrapper> result = getFieldByType(type);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<FieldWrapper> getFieldByType(Class<?> type){
        List<FieldWrapper> result = new ArrayList<>();
        for (FieldWrapper wrapper : fieldCache.values()) {
            if (type.isAssignableFrom(wrapper.getType())) {
                result.add(wrapper);
            }
        }
        return result;
    }


    public MethodWrapper getSingleMethodByAnnotation(Class<? extends Annotation> type){
        List<MethodWrapper> result = getMethodByAnnotation(type);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<MethodWrapper> getMethodByAnnotation(Class<? extends Annotation> type){
        List<MethodWrapper> result = new ArrayList<>();
        for (List<MethodWrapper> wrappers : methodCache.values()) {
            for (MethodWrapper wrapper : wrappers) {
                if (wrapper.hasAnnotation(type)) {
                    result.add(wrapper);
                }
            }
        }
        return result;
    }

    public MethodWrapper getMethod(String name, int argCount){
        List<MethodWrapper> wrappers = methodCache.get(name);
        if (wrappers != null){
            for (MethodWrapper wrapper : wrappers) {
                if (wrapper.getParameterCount() == argCount) {
                    return wrapper;
                }
            }
        }
        return null;
    }

    public MethodWrapper getMethod(String name, Class<?>... types){
        List<MethodWrapper> wrappers = methodCache.get(name);
        if (wrappers != null){
            for (MethodWrapper wrapper : wrappers) {
                if (wrapper.getParameterCount() == types.length) {
                    if (ArrayUtils.equalsArray(types, wrapper.getParameterTypes())) {
                        return wrapper;
                    }
                }
            }
        }
        return null;
    }

    public List<ConstructorWrapper<?>> getConstructorWrapperList(){
        return new ArrayList<>(constructorWrappers.values());
    }

    public ConstructorWrapper<?> getConstructor(Class<?>... types){
        return constructorWrappers.get(new GroupKeys((Object[]) types));
    }

    public ConstructorWrapper<?> getConstructorByAnnotation(Class<? extends Annotation> type){
        for (ConstructorWrapper<?> constructorWrapper : constructorWrappers.values()) {
            if (constructorWrapper.hasAnnotation(type)){
                return constructorWrapper;
            }
        }
        return null;
    }

    public Collection<ConstructorWrapper<?>> getConstructorWrappers() {
        return constructorWrappers.values();
    }

    public static char getPackageSeparator() {
        return PACKAGE_SEPARATOR;
    }

    public List<Constructor<?>> getConstructors(){
        return Arrays.asList(primordialClass.getConstructors());
    }

    public boolean isAssignableFrom(Class<?> cls){
        return primordialClass.isAssignableFrom(cls);
    }

    public ClassLoader getClassLoader(){
        return primordialClass.getClassLoader();
    }

    public T newInstance(){
        try {
            return primordialClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isArray(){
        return primordialClass.isArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof Class) return primordialClass.equals(o);
        if (o instanceof ClassWrapper){
            return ((ClassWrapper<?>)o).primordialClass.equals(primordialClass);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return primordialClass.hashCode();
    }

    @Override
    public Class<T> get() {
        return primordialClass;
    }

    @Override
    public String toString() {
        return "@[" + hashCode() + "]wrapper -> " + get().toString();
    }
}
