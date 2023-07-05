package com.black.javassist;

import com.black.core.query.ClassWrapper;
import com.black.core.util.StringUtils;
import com.black.function.Function;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.utils.IdUtils;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodParametersAttribute;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class WeaverManager {

    private final static Map<String, String> methodIdCache = new ConcurrentHashMap<>();

    private final static Map<String, LinkedBlockingQueue<Weaver>> idWithWeaverQueueCache = new ConcurrentHashMap<>();

    private final static Set<String> loadClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void loadClasses(String packageName, @NonNull Function<CtMethod, Collection<Weaver>> function){
        ChiefScanner scanner = ScannerManager.getScanner();
        List<String> nameList = scanner.fileNameList(packageName);
        for (String name : nameList) {
            if (name.endsWith(".class")){
                String className = getClassName(name);
                loadClass(className, function);
            }
        }
    }

    public static void loadClass(@NonNull String className, @NonNull Function<CtMethod, Collection<Weaver>> function){
        if (isLoadByClassLoader(className) && !loadClasses.contains(className)){
            return;
        }
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.load(className);
        CtMethod[] methods = partiallyCtClass.getRawMethods();
        boolean canLoad = !loadClasses.contains(className);
        for (CtMethod method : methods) {
            if (Modifier.isStatic(method.getModifiers())){
                continue;
            }

            if (method.getDeclaringClass().getName().equals("java.lang.Object")){
                continue;
            }
            String id = handlerMethod(method, function);
            if (id != null){
                if (canLoad){
                    modifyMethodBody(id, method);
                }
            }
        }
        try {
            if (canLoad){
                partiallyCtClass.getJavaClass();
            }
        }finally {
            loadClasses.add(className);
        }
    }

    protected static void modifyMethodBody(String id, CtMethod ctMethod){
        String methodBody = StringUtils.linkStr("com.black.javassist.WeaverManager.callback(", "\"", id, "\"",
                ",", "com.black.core.util.CurrentLineUtils.loadMethod(), this, ", createArgsDesc(ctMethod), ");");
        //System.out.println(methodBody);
        try {
            ctMethod.insertBefore(methodBody);
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static String handlerMethod(CtMethod method, Function<CtMethod, Collection<Weaver>> function){

        try {
            Collection<Weaver> collection = function.apply(method);
            if (collection == null){
                return null;
            }
            String methodDesc = getMethodDesc(method);
            String id = methodIdCache.computeIfAbsent(methodDesc, md -> IdUtils.createShort8Id());
            LinkedBlockingQueue<Weaver> weavers = idWithWeaverQueueCache.computeIfAbsent(id, i -> new LinkedBlockingQueue<>());
            if (collection != null){
                weavers.addAll(collection);
            }
            return id;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }

    }

    public static String createArgsDesc(CtMethod method){
        ConstPool constPool = method.getMethodInfo().getConstPool();
        MethodParametersAttribute attributeInfo = (MethodParametersAttribute) method.getMethodInfo().getAttribute(MethodParametersAttribute.tag);
        if (attributeInfo == null){
            return "new Object[0]";
        }else {
            CtClass[] parameterTypes;
            try {
                 parameterTypes = method.getParameterTypes();
            } catch (NotFoundException e) {
                throw new IllegalStateException("can not find type of method" + getMethodDesc(method), e);
            }

            StringJoiner joiner = new StringJoiner(",", "new Object[]{", "}");
            for (int i = 0; i < attributeInfo.size(); i++) {
                CtClass parameterType = parameterTypes[i];
                String typeName = parameterType.getName();
                boolean basic = ClassWrapper.isBasic(typeName);
                int nameIndex = attributeInfo.name(i);
                String name = constPool.getUtf8Info(nameIndex);
                joiner.add(basic ? wrapperBasicType(typeName, name) : name);
            }
            return joiner.toString();
        }

    }

    public static String wrapperBasicType(String typeName, String name){
        switch (typeName){
            case "int":
                return "java.lang.Integer.valueOf(" + name + ")";
            case "double":
                return "java.lang.Double.valueOf(" + name + ")";
            case "float":
                return "java.lang.Float.valueOf(" + name + ")";
            case "long":
                return "java.lang.Long.valueOf(" + name + ")";
            case "byte":
                return "java.lang.Byte.valueOf(" + name + ")";
            case "short":
                return "java.lang.Short.valueOf(" + name + ")";
            case "boolean":
                return "java.lang.Boolean.valueOf(" + name + ")";
            case "char":
                return "java.lang.Character.valueOf(" + name + ")";
            default:
                throw new IllegalStateException("ill basic type name: " + typeName);
        }
    }

    public static String getClassName(String path){
        String rs;
        return (rs = path.replace('/', '.')).endsWith(".class") ? rs.substring(0, rs.indexOf(".class")) : rs;
    }

    public static String getMethodDesc(CtMethod ctMethod){
        CtClass ctClass = ctMethod.getDeclaringClass();
        String name = ctClass.getName();
        return name + "-" + ctMethod.getName() + "-" + ctMethod.getSignature();
    }


    public static void callback(String id, Method method, Object target, Object[] args){
        LinkedBlockingQueue<Weaver> weavers = idWithWeaverQueueCache.get(id);
        if (weavers != null){
            for (Weaver weaver : weavers) {
                try {
                    weaver.braid(method, target, args);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }


    public static boolean isLoadByClassLoader(String name){
        return isLoadByClassLoader(name, Thread.currentThread().getContextClassLoader());
    }

    public static boolean isLoadByClassLoader(String name, ClassLoader classLoader){
        try {
            Method method = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            method.setAccessible(true);
            Object clazz = method.invoke(classLoader, name);
            return clazz != null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

}
