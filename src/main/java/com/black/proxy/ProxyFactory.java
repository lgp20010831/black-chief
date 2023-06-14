package com.black.proxy;

import com.black.core.query.ClassWrapper;
import com.black.core.util.Assert;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.utils.IdUtils;
import javassist.CtClass;
import javassist.CtConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all") @Getter @Setter
public class ProxyFactory {

    private static Method hascodeMethod;

    private static Method toStringMethod;

    private static Method equalsMethod;

    static {
        try {
            equalsMethod = Object.class.getMethod("equals", Object.class);
            toStringMethod = Object.class.getMethod("toString");
            hascodeMethod = Object.class.getMethod("hashCode");
        } catch (NoSuchMethodException e) {

        }
    }

    private static final Map<Class<?>, Class<?>> cache = new ConcurrentHashMap<>();

    private Class<?> target;

    private boolean useCache;

    private Class<?>[] paramTypes;

    private Object[] createArgs;

    private Class<?>[] interfaces;

    public ProxyFactory(){}

    public ProxyFactory(Class<?> target) {
        this.target = target;
    }

    public Object getProxy(){
        return getProxy0();
    }


    Object getProxy0(){
        Assert.notNull(target, "target is null");
        Class<?> proxyClass;
        if (isUseCache()){
            proxyClass = getClassFromCache();
        }else {
            proxyClass = createProxyClass();
            if (isUseCache()){
                cache.put(target, proxyClass);
            }
        }
    }

    Object instanceByCacheClass(){

    }

    Class<?> getClassFromCache(){
        return cache.get(target);
    }

    Object createProxyClass(){
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(getProxyClassName());
        setRelationship(partiallyCtClass);
        int methodCount = 0;
        Set<Method> methods = getMethods();
        List<Method> methodList = new ArrayList<>(methods);
        for (Method method : methodList) {
            methodCount++;
            createMethod(method, methodCount, partiallyCtClass);
        }
        Class<?>[] array = createConstructor(partiallyCtClass, methodList);
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        Object[] args = getMethodArgs(methodList);

    }

    void setRelationship(PartiallyCtClass partiallyCtClass){
        if (target.isInterface()){
            partiallyCtClass.addInterface(target);
        }else {
            partiallyCtClass.setSuperClass(target);
        }
    }

    Object instanceClass(Class<?> javaClass, Class<?>[] array, Object[] args){
        try {
            Constructor<?> constructor = javaClass.getConstructor(array);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    Class<?>[] createConstructor(PartiallyCtClass partiallyCtClass, List<Method> methods){
        CtClass ctClass = partiallyCtClass.getCtClass();
        List<Class<?>> ctParamTypes = new ArrayList<>();
        if (paramTypes != null){
            ctParamTypes.addAll(Arrays.asList(paramTypes));
        }
        ctParamTypes.add(List.class);
        String body = "{" +
                "super" + getConstrutorDesc() +";\n" +
                getPutMethodDesc(methods) +
                "}";
        Class[] array = ctParamTypes.toArray(new Class[0]);
        partiallyCtClass.addConstructor(body, array);
        return array;
    }



    void createMethod(Method method, int methodCount, PartiallyCtClass partiallyCtClass){
        String genericString = method.toGenericString();
        System.out.println(genericString);
        partiallyCtClass.createField("m" + methodCount, Method.class, null, null);
        String body = "{try{" +
                getMethodReturnDesc(method) + "h.invoke(m" + methodCount + ", this, " + getParamObjectArrayDesc(method) + ");" +
                "}catch(Throwable e){throw new UndeclaredThrowableException(e);}";
        partiallyCtClass.addMethod(method.getName(), method.getReturnType(), body, method.getParameterTypes());
    }

    Object[] getMethodArgs(List<Method> list){
        List<Object> args = new ArrayList<>();
        if (createArgs != null){
            args.addAll(Arrays.asList(createArgs));
        }
        args.add(list);
        return args.toArray();
    }

    String getPutMethodDesc(List<Method> methods){
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < methods.size(); i++) {
            joiner.add("this.m" + i + "=(Method)methods.get(i);");
        }
        return joiner.toString();
    }

    String getConstrutorDesc(){
        if (paramTypes == null){
            return "()";
        }else {
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            for (Class<?> type : paramTypes) {
                joiner.add("$1");
            }
            return joiner.toString();
        }
    }

    String getMethodReturnDesc(Method method){
        if (method.getReturnType().equals(void.class)){
            return "";
        }else {
            return "return (" + method.getReturnType().getSimpleName() + ") ";
        }
    }

    String getParamObjectArrayDesc(Method method){
        StringJoiner joiner = new StringJoiner(",", "new Object[]{", "}");
        for (int i = 0; i < method.getParameterCount(); i++) {
            joiner.add("$" + i);
        }
        return joiner.toString();
    }

    String getProxyClassName(){
        String simpleName = target.getSimpleName();
        simpleName = simpleName + "_ProxyByBlack" + IdUtils.createShort8Id();
        return simpleName;
    }

    Set<Method> getMethods(){
        Set<Method> methods = new LinkedHashSet<>();
        Set<Class<?>> classes = ClassWrapper.getAssignableFromClasses(target);
        for (Class<?> parent : classes) {
            if (parent.equals(Object.class)){
                continue;
            }
            for (Method declaredMethod : parent.getDeclaredMethods()) {
                declaredMethod.setAccessible(true);
                methods.add(declaredMethod);
            }
        }
        return methods;
    }
}
